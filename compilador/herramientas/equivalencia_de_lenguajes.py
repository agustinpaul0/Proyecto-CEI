#!/usr/bin/env python3
"""Verifica que transformar la gramática no haya cambiado el lenguaje.

Clase 4 lo pide explícitamente: al adaptar la gramática para que sea LL(1) hay
que hacerlo «sin cambiar el lenguaje que genera». Eliminar recursión a izquierda
y factorizar son transformaciones que preservan el lenguaje EN TEORÍA; esto lo
comprueba EN LA PRÁCTICA sobre la gramática concreta que se entrega.

Cómo: se deriva una cadena de tokens por CADA producción de cada gramática —y
dos por cada producción anidable, porque un solo uso no distingue `a = b` de
`a = b = c`— y se verifica que la otra gramática la acepte, en las dos
direcciones. Como la gramática base NO es LL(1), no se la puede reconocer con el
simulador del Esquema Simple; se usa un reconocedor de Earley, que reconoce
cualquier gramática libre de contexto sin importar ambigüedad ni recursión a
izquierda.

Además se comprueba el rechazo: se mutan las cadenas generadas (se borra, se
duplica o se cambia un token) y las dos gramáticas tienen que coincidir en el
veredicto.

La cobertura por producción es lo que le da fuerza al chequeo: verificado contra
dos controles negativos, uno que ensancha el lenguaje de la final (permitir
`a = b = c`) y otro que lo angosta (sacar `Sentencia -> Bloque`); los dos se
detectan.

Por último se comprueba que la gramática FINAL, con los logros, contenga todo el
lenguaje de la cátedra: toda cadena derivada de la base, con cada producción
ejercitada, la tiene que aceptar. Los logros agregan construcciones; ninguno puede
quitar una que la base define.

Uso, desde compilador/:
    python3 herramientas/equivalencia_de_lenguajes.py
    python3 herramientas/equivalencia_de_lenguajes.py --extra 400 --semilla 7
"""
import argparse
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from primeros import EPSILON, calcular_vaciables, parsear  # noqa: E402

RAIZ = Path(__file__).resolve().parent.parent
BASE = RAIZ / "gramatica" / "minijava-base.g"
NUCLEO = RAIZ / "gramatica" / "minijava-ll1.g"
FINAL = RAIZ / "gramatica" / "minijava-ll1-final.g"


# --- Reconocedor de Earley ---------------------------------------------------

_INDICES = {}


def _indice(g):
    """El índice de producciones y el conjunto de anulables, cacheados: reconoce()
    se llama miles de veces sobre las mismas dos gramáticas."""
    clave = id(g)
    if clave not in _INDICES:
        producciones = {}
        for izquierda, cuerpo in g.producciones:
            producciones.setdefault(izquierda, []).append(
                tuple(s for s in cuerpo if s != EPSILON))
        _INDICES[clave] = (producciones, calcular_vaciables(g))
    return _INDICES[clave]


def reconoce(g, cadena):
    """True si la gramática deriva exactamente esta lista de terminales.

    Earley: para cada posición i se mantiene el conjunto de items (produccion,
    punto, origen). Predice (el punto está antes de un NT), escanea (antes de un
    terminal que coincide) y completa (el punto llegó al final). No le molesta
    la recursión a izquierda ni la ambigüedad, que es justamente por qué se usa
    acá y no el simulador LL(1).
    """
    producciones, vaciables = _indice(g)

    n = len(cadena)
    columnas = [dict() for _ in range(n + 1)]

    def agregar(columna, item):
        if item not in columnas[columna]:
            columnas[columna][item] = None
            return True
        return False

    for cuerpo in producciones[g.inicial]:
        agregar(0, (g.inicial, cuerpo, 0, 0))

    for i in range(n + 1):
        pendientes = list(columnas[i])
        while pendientes:
            item = pendientes.pop()
            izquierda, cuerpo, punto, origen = item
            if punto == len(cuerpo):
                for previo in list(columnas[origen]):
                    pi, pc, pp, po = previo
                    if pp < len(pc) and pc[pp] == izquierda:
                        if agregar(i, (pi, pc, pp + 1, po)):
                            pendientes.append((pi, pc, pp + 1, po))
                continue
            siguiente = cuerpo[punto]
            if siguiente in producciones:
                for produccion in producciones[siguiente]:
                    if agregar(i, (siguiente, produccion, 0, i)):
                        pendientes.append((siguiente, produccion, 0, i))
                # Predicción con NT anulable: hay que avanzar el punto ya, o el
                # item se pierde cuando el NT deriva a vacío (corrección de Aycock
                # y Horspool al algoritmo original).
                if siguiente in vaciables and agregar(i, (izquierda, cuerpo, punto + 1, origen)):
                    pendientes.append((izquierda, cuerpo, punto + 1, origen))
            elif i < n and siguiente == cadena[i]:
                agregar(i + 1, (izquierda, cuerpo, punto + 1, origen))

    return any(izquierda == g.inicial and punto == len(cuerpo) and origen == 0
               for izquierda, cuerpo, punto, origen in columnas[n])


# --- Generador de cadenas ----------------------------------------------------

def _cuerpos(g):
    cuerpos = {}
    for izquierda, cuerpo in g.producciones:
        cuerpos.setdefault(izquierda, []).append(
            [s for s in cuerpo if s != EPSILON])
    return cuerpos


def _alcanzables(cuerpos):
    """Qué no terminales se pueden alcanzar desde cada uno. Sirve para dirigir la
    derivación hacia la producción que se quiere ejercitar en vez de esperar a
    que el azar la encuentre."""
    directos = {nt: {s for cuerpo in bodies for s in cuerpo if s in cuerpos}
                for nt, bodies in cuerpos.items()}
    alcance = {nt: set(hijos) for nt, hijos in directos.items()}
    cambio = True
    while cambio:
        cambio = False
        for nt, hijos in alcance.items():
            nuevo = set(hijos)
            for hijo in hijos:
                nuevo |= alcance.get(hijo, set())
            if nuevo != hijos:
                alcance[nt] = nuevo
                cambio = True
    return alcance


def _minimos(cuerpos):
    """Longitud mínima en terminales que cada no terminal puede derivar. Sirve
    para cortar una derivación por el camino más corto que seguro termina."""
    minimo = {nt: float("inf") for nt in cuerpos}
    cambio = True
    while cambio:
        cambio = False
        for nt, bodies in cuerpos.items():
            mejor = min((sum(minimo.get(s, 1) for s in cuerpo) for cuerpo in bodies),
                        default=float("inf"))
            if mejor < minimo[nt]:
                minimo[nt] = mejor
                cambio = True
    return minimo


class Generador:
    """Deriva cadenas de terminales. Con `objetivo` fuerza que la derivación use
    esa producción: se dirige hacia su no terminal bajando por el camino más corto
    del grafo de alcance, así que llega siempre y termina siempre."""

    def __init__(self, g, azar, profundidad_maxima=14, largo_maximo=140):
        self.cuerpos = _cuerpos(g)
        self.directos = {nt: {s for bodies in [b] for cuerpo in bodies for s in cuerpo
                              if s in self.cuerpos}
                         for nt, b in self.cuerpos.items()}
        self.minimo = _minimos(self.cuerpos)
        self.inicial = g.inicial
        self.azar = azar
        self.profundidad_maxima = profundidad_maxima
        self.largo_maximo = largo_maximo
        self._distancias = {}

    def todas_las_producciones(self):
        return [(nt, tuple(cuerpo))
                for nt, bodies in self.cuerpos.items() for cuerpo in bodies]

    def _distancia_a(self, objetivo):
        """dist[X] = cuántas expansiones mínimas hacen falta desde X para llegar a
        `objetivo`. Bajar siempre por una distancia estrictamente menor garantiza
        que la derivación llega y no da vueltas."""
        if objetivo not in self._distancias:
            dist = {objetivo: 0}
            cambio = True
            while cambio:
                cambio = False
                for nt, hijos in self.directos.items():
                    alcanzable = [dist[h] for h in hijos if h in dist]
                    if alcanzable and dist.get(nt, float("inf")) > min(alcanzable) + 1:
                        dist[nt] = min(alcanzable) + 1
                        cambio = True
            self._distancias[objetivo] = dist
        return self._distancias[objetivo]

    def _mas_cortos(self, cuerpos):
        peso = lambda c: sum(self.minimo.get(s, 1) for s in c)
        mejor = min(peso(c) for c in cuerpos)
        return [c for c in cuerpos if peso(c) == mejor]

    def es_recursiva(self, produccion):
        """La producción puede volver a su propio no terminal, o sea que se puede
        anidar. Son las que hay que ejercitar DOS veces: un solo uso no distingue
        `a = b` de `a = b = c`."""
        dist = self._distancia_a(produccion[0])
        return any(s in dist for s in produccion[1])

    def generar(self, objetivo=None, veces=1):
        """Deriva una cadena. Con `objetivo`, persigue esa producción bajando por
        UNA sola posición del cuerpo elegido —la que acorta la distancia— y deriva
        el resto normalmente. Perseguir una sola posición es lo que hace que la
        recursión a izquierda de la gramática base no lleve a un descenso infinito.

        `veces` exige que la producción se use esa cantidad de veces anidadas.
        """
        salida = []
        restantes = [veces if objetivo else 0]
        dist = self._distancia_a(objetivo[0]) if objetivo else {}
        if objetivo and self.inicial not in dist:
            return None

        def derivar(simbolo, profundidad, persiguiendo):
            if len(salida) > self.largo_maximo:
                return False
            if simbolo not in self.cuerpos:
                salida.append(simbolo)
                return True

            if persiguiendo and simbolo == objetivo[0]:
                cuerpo = list(objetivo[1])
                restantes[0] -= 1
                posiciones = ([i for i, s in enumerate(cuerpo) if s in dist]
                              if restantes[0] else [])
                for k in (self.azar.sample(posiciones, len(posiciones)) or [None]):
                    marca = len(salida)
                    if all(derivar(s, profundidad + 1, i == k)
                           for i, s in enumerate(cuerpo)):
                        return True
                    del salida[marca:]
                restantes[0] += 1
                return False

            cuerpos = list(self.cuerpos[simbolo])
            objetivo_distancia = dist.get(simbolo, 0) - 1 if persiguiendo else None
            if persiguiendo:
                cuerpos = [c for c in cuerpos
                           if any(dist.get(s) == objetivo_distancia for s in c)] or None
                if cuerpos is None:
                    return False
            elif profundidad >= self.profundidad_maxima:
                cuerpos = self._mas_cortos(cuerpos)

            for cuerpo in self.azar.sample(cuerpos, len(cuerpos)):
                if persiguiendo:
                    posiciones = [i for i, s in enumerate(cuerpo)
                                  if dist.get(s) == objetivo_distancia]
                else:
                    posiciones = [None]
                for k in self.azar.sample(posiciones, len(posiciones)):
                    marca, antes = len(salida), restantes[0]
                    if all(derivar(s, profundidad + 1, i == k)
                           for i, s in enumerate(cuerpo)):
                        return True
                    del salida[marca:]
                    restantes[0] = antes
            return False

        try:
            ok = derivar(self.inicial, 0, objetivo is not None)
        except RecursionError:
            return None
        return salida if ok and restantes[0] == 0 else None


def mutar(cadena, terminales, azar):
    if not cadena:
        return None
    copia = list(cadena)
    i = azar.randrange(len(copia))
    operacion = azar.choice(("borrar", "duplicar", "cambiar"))
    if operacion == "borrar":
        del copia[i]
    elif operacion == "duplicar":
        copia.insert(i, copia[i])
    else:
        copia[i] = azar.choice(terminales)
    return copia


# --- Comparación -------------------------------------------------------------

def comparar(base, final, extra, semilla):
    """Ejercita TODA producción de cada gramática y comprueba que la otra esté de
    acuerdo, más `extra` cadenas al azar por gramática y un mutante por cadena."""
    azar = random.Random(semilla)
    terminales = sorted(set(base.terminales()) | set(final.terminales()))
    fallos = []
    resumen = {}
    mutadas = 0

    for nombre, g_origen, g_otra in (("base", base, final), ("final", final, base)):
        generador = Generador(g_origen, azar)
        objetivos = generador.todas_las_producciones()
        cubiertas = 0
        sin_alcanzar = []
        cadenas = []

        for objetivo in objetivos:
            for veces in (1, 2) if generador.es_recursiva(objetivo) else (1,):
                cadena = generador.generar(objetivo, veces)
                if cadena is None:
                    sin_alcanzar.append(
                        f"{objetivo[0]} -> {' '.join(objetivo[1]) or 'EPSILON'}")
                    continue
                cubiertas += 1
                cadenas.append(cadena)

        for _ in range(extra):
            cadena = generador.generar()
            if cadena is not None:
                cadenas.append(cadena)

        for cadena in cadenas:
            if not reconoce(g_otra, cadena):
                fallos.append((nombre, "la otra gramatica la RECHAZA", cadena))
                continue
            mutante = mutar(cadena, terminales, azar)
            if mutante is None:
                continue
            mutadas += 1
            if reconoce(base, mutante) != reconoce(final, mutante):
                cual = "solo la base" if reconoce(base, mutante) else "solo la final"
                fallos.append((nombre, f"mutante aceptado por {cual}", mutante))

    resumen = {"mutadas": mutadas}
    return resumen, fallos


def perdidas_por_la_final(base, final, extra, semilla):
    """Las cadenas de la base que la gramática final rechaza."""
    azar = random.Random(semilla)
    generador = Generador(base, azar)
    cadenas = []
    for objetivo in generador.todas_las_producciones():
        for veces in (1, 2) if generador.es_recursiva(objetivo) else (1,):
            cadena = generador.generar(objetivo, veces)
            if cadena is not None:
                cadenas.append(cadena)
    for _ in range(extra):
        cadena = generador.generar()
        if cadena is not None:
            cadenas.append(cadena)
    return len(cadenas), [c for c in cadenas if not reconoce(final, c)]


def cobertura(g, semilla):
    """Cuántas producciones se pudieron ejercitar, cuáles quedaron inertes por ser
    de un no terminal inalcanzable, y cuáles no se pudieron ejercitar sin
    explicación."""
    generador = Generador(g, random.Random(semilla))
    total = generador.todas_las_producciones()
    inalcanzables = set(g.inalcanzables())
    inertes, sin_ejercitar = [], []
    for objetivo in total:
        veces = 2 if generador.es_recursiva(objetivo) else 1
        if generador.generar(objetivo, veces) is not None:
            continue
        if veces == 2 and generador.generar(objetivo) is not None:
            continue
        (inertes if objetivo[0] in inalcanzables else sin_ejercitar).append(objetivo)
    return len(total), inertes, sin_ejercitar


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--extra", type=int, default=100,
                    help="cadenas al azar adicionales por gramatica (default 100)")
    ap.add_argument("--semilla", type=int, default=20260908)
    args = ap.parse_args()

    base = parsear(BASE.read_text(encoding="utf-8"))
    # El núcleo LL(1) sin logros: es la gramática que tiene que reproducir
    # especificación de la cátedra.
    final = parsear(NUCLEO.read_text(encoding="utf-8"))

    fallos_de_cobertura = 0
    for nombre, g in (("minijava-base.g", base), ("minijava-ll1.g", final)):
        total, inertes, sin_ejercitar = cobertura(g, args.semilla)
        print(f"{nombre}: {total - len(inertes) - len(sin_ejercitar)}/{total} "
              f"producciones ejercitadas, {len(inertes)} inertes")
        for produccion in inertes:
            print(f"    inerte         {produccion[0]} -> {' '.join(produccion[1]) or 'EPSILON'}")
        for produccion in sin_ejercitar:
            print(f"    SIN EJERCITAR  {produccion[0]} -> {' '.join(produccion[1]) or 'EPSILON'}")
        fallos_de_cobertura += len(sin_ejercitar)

    resumen, fallos = comparar(base, final, args.extra, args.semilla)

    for origen, motivo, cadena in fallos[:20]:
        print(f"  DIVERGE  derivada de la {origen}: {motivo}")
        print(f"           {' '.join(cadena)}")

    print(f"\n{resumen['mutadas']} mutantes comparados en las dos gramaticas.")
    if fallos or fallos_de_cobertura:
        print(f"{len(fallos)} DIVERGENCIA(S) DE LENGUAJE, "
              f"{fallos_de_cobertura} PRODUCCION(ES) SIN EJERCITAR.")
        return 1
    print("LAS DOS GRAMATICAS GENERAN EL MISMO LENGUAJE: "
          "toda produccion de cada una fue ejercitada y la otra estuvo de acuerdo.")

    total, perdidas = perdidas_por_la_final(
        base, parsear(FINAL.read_text(encoding="utf-8")), args.extra * 10, args.semilla)
    for cadena in perdidas[:20]:
        print(f"  PERDIDA  {' '.join(cadena)}")
    if perdidas:
        print(f"{len(perdidas)} de {total} cadenas de la base RECHAZADAS por la gramatica final.")
        return 1
    print(f"LA GRAMATICA FINAL CONTIENE A LA BASE: acepta las {total} cadenas derivadas de ella.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
