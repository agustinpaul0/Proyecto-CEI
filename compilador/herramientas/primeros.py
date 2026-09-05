#!/usr/bin/env python3
"""Calcula Primeros y verifica la condición LL(1) de una gramática.

Andamiaje, no parte del compilador. Existe para **contrastar** la cuenta hecha a
mano en el informe, no para reemplazarla: si el script y la cuenta manual
difieren, hay que entender cuál de los dos se equivocó antes de seguir.

Formato de entrada (.g): una producción por línea,

    Inicial -> ListaClases eof
    ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON

Un símbolo es NO TERMINAL si aparece alguna vez del lado izquierdo de una
flecha; todo lo demás es terminal. `EPSILON` es la cadena vacía. Las líneas que
empiezan con `#` y las vacías se ignoran.

Uso, siempre desde `compilador/`:

    python3 herramientas/primeros.py gramatica/minijava-ll1.g
    python3 herramientas/primeros.py gramatica/minijava-ll1.g --nt Miembro
    python3 herramientas/primeros.py gramatica/minijava-ll1.g --markdown
"""

from __future__ import annotations

import argparse
import re
import sys
from collections import OrderedDict

EPSILON = "EPSILON"


class Gramatica:
    def __init__(self, producciones, inicial):
        # producciones: lista de (izquierda, [simbolos]) en orden de aparición
        self.producciones = producciones
        self.inicial = inicial
        self.no_terminales = OrderedDict()
        for izquierda, _ in producciones:
            self.no_terminales.setdefault(izquierda, None)
        self.por_izquierda = OrderedDict()
        for izquierda, cuerpo in producciones:
            self.por_izquierda.setdefault(izquierda, []).append(cuerpo)

    def es_no_terminal(self, simbolo):
        return simbolo in self.no_terminales

    def terminales(self):
        vistos = OrderedDict()
        for _, cuerpo in self.producciones:
            for simbolo in cuerpo:
                if simbolo != EPSILON and not self.es_no_terminal(simbolo):
                    vistos.setdefault(simbolo, None)
        return list(vistos)

    def no_definidos(self):
        """No terminales usados en un cuerpo pero sin producciones propias.

        Con la convención de «no terminal = aparece a la izquierda» esto no
        puede pasar por definición, así que reportamos lo contrario: símbolos
        capitalizados que parecen no terminales pero se están tomando como
        terminales. Casi siempre es un typo.
        """
        sospechosos = OrderedDict()
        for _, cuerpo in self.producciones:
            for simbolo in cuerpo:
                if simbolo == EPSILON or self.es_no_terminal(simbolo):
                    continue
                if simbolo[:1].isupper():
                    sospechosos.setdefault(simbolo, None)
        return list(sospechosos)

    def inalcanzables(self):
        alcanzados = set()
        pendientes = [self.inicial]
        while pendientes:
            actual = pendientes.pop()
            if actual in alcanzados:
                continue
            alcanzados.add(actual)
            for cuerpo in self.por_izquierda.get(actual, []):
                for simbolo in cuerpo:
                    if self.es_no_terminal(simbolo):
                        pendientes.append(simbolo)
        return [nt for nt in self.no_terminales if nt not in alcanzados]


def dividir_en_alternativas(tokens):
    """Parte una lista de tokens en los `|` exactos. `||` no es separador."""
    alternativas = [[]]
    for token in tokens:
        if token == "|":
            alternativas.append([])
        else:
            alternativas[-1].append(token)
    return alternativas


def parsear(texto, logros=None):
    """Parsea la gramática, quedándose con las producciones activas.

    Una producción puede llevar una marca al final:
        @+nombre   existe sólo si el logro `nombre` está PRENDIDO
        @-nombre   existe sólo si el logro `nombre` está APAGADO
    Sin marca, existe siempre.

    `logros` es el conjunto de logros prendidos; None significa todos.
    """
    producciones = []
    inicial = None
    for numero, linea in enumerate(texto.splitlines(), start=1):
        linea = linea.split("#", 1)[0].strip()
        if not linea:
            continue
        marca = None
        if "@" in linea:
            linea, _, marca = linea.partition("@")
            linea, marca = linea.strip(), marca.strip()
            if marca[:1] not in "+-" or len(marca) < 2:
                raise SystemExit(f"linea {numero}: marca invalida '@{marca}'")
        if marca is not None and logros is not None:
            prendido = marca[1:] in logros
            if (marca[0] == "+") != prendido:
                continue
        if "->" not in linea:
            raise SystemExit(f"linea {numero}: falta '->' en: {linea}")
        izquierda, derecha = linea.split("->", 1)
        izquierda = izquierda.strip()
        if not izquierda:
            raise SystemExit(f"linea {numero}: lado izquierdo vacio")
        if inicial is None:
            inicial = izquierda
        # Se tokeniza por espacios ANTES de separar alternativas: así el `|`
        # separador (un token exacto) no se confunde con el terminal `||` de
        # MiniJava. Corolario del formato: los símbolos van siempre separados
        # por espacios, y un `|` suelto es siempre separador, nunca terminal.
        for alternativa in dividir_en_alternativas(derecha.split()):
            cuerpo = alternativa
            if not cuerpo:
                raise SystemExit(f"linea {numero}: alternativa vacia (usa EPSILON)")
            if EPSILON in cuerpo and len(cuerpo) > 1:
                raise SystemExit(f"linea {numero}: EPSILON no se mezcla con otros simbolos")
            producciones.append((izquierda, cuerpo))
    if inicial is None:
        raise SystemExit("gramatica vacia")
    return Gramatica(producciones, inicial)


def calcular_vaciables(g):
    """X es vaciable si X ⇒* ε (Clase 3, slide 61). Punto fijo."""
    vaciables = set()
    cambio = True
    while cambio:
        cambio = False
        for izquierda, cuerpo in g.producciones:
            if izquierda in vaciables:
                continue
            if cuerpo == [EPSILON] or all(
                s in vaciables for s in cuerpo if g.es_no_terminal(s)
            ) and all(g.es_no_terminal(s) for s in cuerpo):
                vaciables.add(izquierda)
                cambio = True
    return vaciables


def calcular_primeros_nt(g, vaciables):
    """Primeros de cada no terminal, por punto fijo."""
    primeros = {nt: set() for nt in g.no_terminales}
    cambio = True
    while cambio:
        cambio = False
        for izquierda, cuerpo in g.producciones:
            antes = len(primeros[izquierda])
            primeros[izquierda] |= primeros_de_secuencia(g, primeros, vaciables, cuerpo) - {
                EPSILON
            }
            if len(primeros[izquierda]) != antes:
                cambio = True
    return primeros


def primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo):
    """Primeros(β) para una secuencia β. Incluye EPSILON si β ⇒* ε por completo."""
    resultado = set()
    for simbolo in cuerpo:
        if simbolo == EPSILON:
            resultado.add(EPSILON)
            return resultado
        if not g.es_no_terminal(simbolo):
            resultado.add(simbolo)
            return resultado
        resultado |= primeros_nt[simbolo]
        if simbolo not in vaciables:
            return resultado
    resultado.add(EPSILON)
    return resultado


def longitudes_derivables(g):
    """(mínimo, máximo) de tokens que cada NT puede derivar. máximo = None si es infinito.

    Sirve para separar dos defectos que se confunden fácil: dos producciones que
    comparten prefijo izquierdo rompen LL(1), pero **sólo son ambiguas si sus
    lenguajes se intersecan**. Si los rangos de longitud son disjuntos, no hay
    ninguna cadena con dos árboles y el defecto es sólo de predicción.
    """
    INF = float("inf")
    minimo = {nt: INF for nt in g.no_terminales}
    for _ in range(len(g.no_terminales) + 2):
        for izquierda, cuerpo in g.producciones:
            if cuerpo == [EPSILON]:
                total = 0
            else:
                total = sum(
                    minimo[s] if g.es_no_terminal(s) else 1 for s in cuerpo
                )
            if total < minimo[izquierda]:
                minimo[izquierda] = total

    # Máximo: infinito si el NT es (directa o indirectamente) recursivo.
    depende = {nt: set() for nt in g.no_terminales}
    for izquierda, cuerpo in g.producciones:
        for s in cuerpo:
            if g.es_no_terminal(s):
                depende[izquierda].add(s)
    recursivos = set()
    for nt in g.no_terminales:
        vistos, pendientes = set(), list(depende[nt])
        while pendientes:
            actual = pendientes.pop()
            if actual in vistos:
                continue
            vistos.add(actual)
            pendientes.extend(depende[actual])
        if nt in vistos:
            recursivos.add(nt)

    maximo = {}
    for nt in g.no_terminales:
        if nt in recursivos:
            maximo[nt] = None
            continue
        mejor = 0
        for cuerpo in g.por_izquierda[nt]:
            if cuerpo == [EPSILON]:
                total = 0
            else:
                total = sum(
                    (maximo.get(s) if g.es_no_terminal(s) else 1) or 0 for s in cuerpo
                )
            mejor = max(mejor, total)
        maximo[nt] = mejor
    # Segunda pasada: los máximos dependen del orden de resolución.
    for _ in range(len(g.no_terminales) + 2):
        for nt in g.no_terminales:
            if maximo[nt] is None:
                continue
            mejor = 0
            for cuerpo in g.por_izquierda[nt]:
                if cuerpo == [EPSILON]:
                    total = 0
                else:
                    partes = [maximo[s] if g.es_no_terminal(s) else 1 for s in cuerpo]
                    if any(p is None for p in partes):
                        total = None
                    else:
                        total = sum(partes)
                if total is None:
                    mejor = None
                    break
                mejor = max(mejor, total)
            maximo[nt] = mejor
    return minimo, maximo


def formatear(conjunto):
    if not conjunto:
        return "∅"
    ordenados = sorted(c for c in conjunto if c != EPSILON)
    if EPSILON in conjunto:
        ordenados.append("ε")
    return "{ " + ", ".join(ordenados) + " }"


def analizar(g):
    vaciables = calcular_vaciables(g)
    primeros_nt = calcular_primeros_nt(g, vaciables)
    conflictos = []
    for nt, cuerpos in g.por_izquierda.items():
        if len(cuerpos) < 2:
            continue
        conjuntos = [
            (cuerpo, primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo))
            for cuerpo in cuerpos
        ]
        for i in range(len(conjuntos)):
            for j in range(i + 1, len(conjuntos)):
                choque = conjuntos[i][1] & conjuntos[j][1]
                if choque:
                    conflictos.append((nt, conjuntos[i][0], conjuntos[j][0], choque))
    return vaciables, primeros_nt, conflictos


def aceptar(g, vaciables, primeros_nt, tokens):
    """Como `aceptar_detallado`, pero sin la posición del token que falló."""
    ok, motivo, _ = aceptar_detallado(g, vaciables, primeros_nt, tokens)
    return ok, motivo


def aceptar_detallado(g, vaciables, primeros_nt, tokens):
    """Simula el Esquema Simple de Clase 5 sobre una secuencia de tokens.

    Es el mismo algoritmo que va a tener el parser: para un NT se elige la
    producción que tenga al token actual en los Primeros de su cuerpo, y si
    ninguna lo tiene pero el NT es vaciable, no se hace nada (slides 52-54).
    Sirve para comprobar que la gramática acepta lo que los logros piden y
    rechaza lo que prohíben, antes de escribir una línea de Java.
    """
    pila = [g.inicial]
    i = 0
    while pila:
        tope = pila.pop()
        if tope == EPSILON:
            continue
        actual = tokens[i] if i < len(tokens) else None
        if not g.es_no_terminal(tope):
            if actual == tope:
                i += 1
                continue
            return False, f"se esperaba `{tope}` y se encontro `{actual or 'fin de entrada'}`", i
        elegida = None
        for cuerpo in g.por_izquierda[tope]:
            if actual in primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo):
                elegida = cuerpo
                break
        if elegida is None:
            if tope in vaciables:
                continue          # <NT> derivo a vacio: no hago nada
            return False, f"en <{tope}> ninguna produccion admite `{actual or 'fin de entrada'}`", i
        for simbolo in reversed(elegida):
            if simbolo != EPSILON:
                pila.append(simbolo)
    if i != len(tokens):
        return False, f"sobraron tokens desde `{tokens[i]}`", i
    return True, "aceptada", i


def verificar_combinatoria(texto, disponibles):
    """LL(1) en las 2^n configuraciones de logros. Es la garantía de atomicidad:
    ningún logro puede depender de que otro esté prendido."""
    total = 1 << len(disponibles)
    print(f"{len(disponibles)} logros -> {total} configuraciones\n")
    fallidas = []
    for mascara in range(total):
        encendidos = {
            nombre for i, nombre in enumerate(disponibles) if mascara & (1 << i)
        }
        g = parsear(texto, encendidos)
        _, _, conflictos = analizar(g)
        if conflictos:
            fallidas.append((encendidos, conflictos))
    if fallidas:
        print(f"FALLAN {len(fallidas)} de {total} configuraciones:\n")
        for encendidos, conflictos in fallidas[:10]:
            etiqueta = ", ".join(sorted(encendidos)) or "(ninguno)"
            print(f"  con {{{etiqueta}}}: {len(conflictos)} conflicto(s)")
            for nt, uno, otro, choque in conflictos[:3]:
                print(f"      <{nt}>: {' '.join(uno)}  /  {' '.join(otro)}")
                print(f"          interseccion = {formatear(choque)}")
            print()
        return 1
    print(f"LAS {total} CONFIGURACIONES SON LL(1).")
    print("Cada logro se puede prender y apagar de forma independiente.")
    return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("gramatica")
    parser.add_argument("--nt", help="mostrar solo este no terminal")
    parser.add_argument("--markdown", action="store_true", help="tabla lista para el informe")
    parser.add_argument(
        "--logros",
        help="lista separada por comas de logros PRENDIDOS; 'ninguno' o 'todos'",
    )
    parser.add_argument(
        "--aceptar",
        metavar="TOKENS",
        help="simula el parser LL(1) sobre una secuencia de tokens separados por espacios",
    )
    parser.add_argument(
        "--combinatoria",
        action="store_true",
        help="verifica LL(1) en TODA combinacion de logros (2^n configuraciones)",
    )
    parser.add_argument(
        "--longitudes",
        nargs="*",
        metavar="NT",
        help="rango de longitud derivable; con dos NT dice si sus lenguajes pueden intersecarse",
    )
    args = parser.parse_args()

    with open(args.gramatica, encoding="utf-8") as archivo:
        texto = archivo.read()

    disponibles = sorted(set(re.findall(r"@[+-](\w+)", texto)))

    if args.combinatoria:
        return verificar_combinatoria(texto, disponibles)

    if args.logros is None or args.logros == "todos":
        encendidos = set(disponibles)
    elif args.logros == "ninguno":
        encendidos = set()
    else:
        encendidos = {x.strip() for x in args.logros.split(",") if x.strip()}
        desconocidos = encendidos - set(disponibles)
        if desconocidos:
            raise SystemExit(
                f"logro(s) desconocido(s): {', '.join(sorted(desconocidos))}\n"
                f"disponibles: {', '.join(disponibles)}"
            )
    if disponibles:
        prendidos = ", ".join(sorted(encendidos)) if encendidos else "(ninguno)"
        print(f"Logros prendidos: {prendidos}\n")
    g = parsear(texto, encendidos if disponibles else None)

    vaciables, primeros_nt, conflictos = analizar(g)

    if args.aceptar is not None:
        ok, motivo = aceptar(g, vaciables, primeros_nt, args.aceptar.split())
        print(("ACEPTADA" if ok else "RECHAZADA") + f": {motivo}")
        return 0 if ok else 1

    if args.longitudes is not None:
        minimo, maximo = longitudes_derivables(g)
        objetivo = args.longitudes or list(g.no_terminales)
        for nt in objetivo:
            if nt not in g.no_terminales:
                raise SystemExit(f"no terminal desconocido: {nt}")
            techo = "∞" if maximo[nt] is None else maximo[nt]
            print(f"<{nt}>: entre {minimo[nt]} y {techo} tokens")
        if len(objetivo) == 2:
            a, b = objetivo
            techo_a = float("inf") if maximo[a] is None else maximo[a]
            techo_b = float("inf") if maximo[b] is None else maximo[b]
            if techo_a < minimo[b] or techo_b < minimo[a]:
                print(
                    f"\nLos rangos NO se solapan: ninguna cadena puede estar en los dos\n"
                    f"lenguajes, asi que <{a}> y <{b}> NO son ambiguos entre si.\n"
                    f"Si comparten prefijo izquierdo es un conflicto de PREDICCION (LL(1)),\n"
                    f"no de ambiguedad, y se arregla factorizando."
                )
            else:
                print(
                    f"\nLos rangos se solapan: por longitud no se puede descartar que\n"
                    f"<{a}> y <{b}> deriven una misma cadena. Hace falta otro argumento."
                )
        return 0

    sospechosos = g.no_definidos()
    if sospechosos:
        print("AVISO — simbolos capitalizados tomados como TERMINALES (¿typo?):")
        for s in sospechosos:
            print(f"  {s}")
        print()

    huerfanos = g.inalcanzables()
    if huerfanos:
        print(f"AVISO — no terminales inalcanzables desde <{g.inicial}>:")
        for s in huerfanos:
            print(f"  {s}")
        print()

    nts = [args.nt] if args.nt else list(g.no_terminales)
    if args.nt and args.nt not in g.no_terminales:
        raise SystemExit(f"no terminal desconocido: {args.nt}")

    if args.markdown:
        print("| No terminal | Producción | Primeros |")
        print("|---|---|---|")
        for nt in nts:
            for cuerpo in g.por_izquierda[nt]:
                conjunto = primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo)
                derecha = " ".join(cuerpo).replace(EPSILON, "ε")
                print(f"| `<{nt}>` | `{derecha}` | {formatear(conjunto)} |")
    else:
        for nt in nts:
            marca = "  (vaciable)" if nt in vaciables else ""
            print(f"<{nt}>{marca}")
            for cuerpo in g.por_izquierda[nt]:
                conjunto = primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo)
                derecha = " ".join(cuerpo).replace(EPSILON, "ε")
                print(f"    ::= {derecha}")
                print(f"        Primeros = {formatear(conjunto)}")
            print()

    print(f"--- {len(g.no_terminales)} no terminales, "
          f"{len(g.producciones)} producciones, "
          f"{len(g.terminales())} terminales ---")

    if conflictos:
        print(f"\nNO ES LL(1): {len(conflictos)} conflicto(s)\n")
        for nt, uno, otro, choque in conflictos:
            print(f"  <{nt}>")
            print(f"      ::= {' '.join(uno).replace(EPSILON, 'ε')}")
            print(f"      ::= {' '.join(otro).replace(EPSILON, 'ε')}")
            print(f"      interseccion = {formatear(choque)}\n")
        return 1

    print("\nES LL(1): toda produccion de todo no terminal tiene Primeros disjuntos.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
