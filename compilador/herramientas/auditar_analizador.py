#!/usr/bin/env python3
"""Audita que el analizador y la gramatica no hayan divergido.

Cada metodo de no terminal cita arriba las producciones que traduce. Este
script compara esas citas contra gramatica/minijava-ll1-final.g y falla si:

  - una produccion de la gramatica no la cita ningun metodo (esta sin implementar
    o quedo huerfana);
  - un metodo cita una produccion que la gramatica ya no tiene (el comentario
    quedo viejo);
  - una cita no coincide literalmente con la produccion, simbolo por simbolo.

Uso, desde compilador/:
    python3 herramientas/auditar_analizador.py
"""
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from primeros import EPSILON, dividir_en_alternativas, parsear  # noqa: E402

RAIZ = Path(__file__).resolve().parent.parent
GRAMATICA = RAIZ / "gramatica" / "minijava-ll1-final.g"
PARTES = RAIZ / "src" / "main" / "java" / "minijava" / "sintactico" / "partes"

def normalizar(izquierda, cuerpo):
    simbolos = [s for s in cuerpo if s != EPSILON] or ["EPSILON"]
    return f"{izquierda} -> {' '.join(simbolos)}"


def leer_gramatica():
    return parsear(GRAMATICA.read_text(encoding="utf-8"))


def producciones_de_la_gramatica(g):
    return {normalizar(izquierda, cuerpo) for izquierda, cuerpo in g.producciones}


def vocabulario(g):
    """Todo simbolo que la gramatica conoce, para distinguir una continuacion de
    una cita de una linea de prosa: la prosa siempre trae alguna palabra que no
    es un simbolo."""
    simbolos = set(g.no_terminales) | {"EPSILON"}
    for _, cuerpo in g.producciones:
        simbolos.update(s for s in cuerpo if s != EPSILON)
    return simbolos


def citas_del_codigo(alfabeto):
    citas = {}
    for archivo in sorted(PARTES.glob("*.java")):
        izquierda = None
        ultima = None  # (clave sin cerrar, numero de linea)
        for numero, linea in enumerate(archivo.read_text(encoding="utf-8").splitlines(), 1):
            texto = linea.strip()
            if not texto.startswith("//"):
                izquierda, ultima = None, None
                continue
            texto = texto[2:].strip()

            if "->" in texto:
                cabeza, cuerpo = texto.split("->", 1)
                cabeza = cabeza.strip()
                if not re.fullmatch(r"[A-Z][A-Za-z]*", cabeza):
                    izquierda, ultima = None, None
                    continue
                izquierda = cabeza
            elif texto.startswith("|") and izquierda:
                cuerpo = texto[1:]
            elif ultima and texto and all(s in alfabeto for s in texto.split()):
                # Continuacion de la produccion anterior, partida por el ancho de linea.
                clave, donde = ultima
                citas[clave].remove(donde)
                if not citas[clave]:
                    del citas[clave]
                clave = f"{clave} {' '.join(texto.split())}"
                citas.setdefault(clave, []).append(donde)
                ultima = (clave, donde)
                continue
            else:
                izquierda, ultima = None, None
                continue

            ultima = None
            # Se tokeniza por espacios ANTES de partir en alternativas, igual que
            # la gramática: si no, el terminal `||` se confundiría con el separador.
            for simbolos in dividir_en_alternativas(cuerpo.split()):
                if not simbolos:
                    continue
                clave = f"{izquierda} -> {' '.join(simbolos)}"
                donde = f"{archivo.name}:{numero}"
                citas.setdefault(clave, []).append(donde)
                ultima = (clave, donde)
    return citas


def main():
    g = leer_gramatica()
    gramatica = producciones_de_la_gramatica(g)
    citas = citas_del_codigo(vocabulario(g))

    sin_implementar = sorted(gramatica - set(citas))
    sin_respaldo = sorted(set(citas) - gramatica)

    for produccion in sin_implementar:
        print(f"  SIN CITAR   {produccion}")
    for produccion in sin_respaldo:
        print(f"  CITA VIEJA  {produccion}   ({', '.join(citas[produccion])})")

    print(f"\n{len(gramatica)} producciones en la gramatica, "
          f"{len(set(citas) & gramatica)} citadas por el analizador.")
    fallos = len(sin_implementar) + len(sin_respaldo)
    if fallos == 0:
        print("EL ANALIZADOR CITA EXACTAMENTE LAS PRODUCCIONES DE LA GRAMATICA.")
    else:
        print(f"{fallos} DIVERGENCIA(S) ENTRE EL CODIGO Y LA GRAMATICA.")
    return fallos


if __name__ == "__main__":
    sys.exit(min(main(), 1))
