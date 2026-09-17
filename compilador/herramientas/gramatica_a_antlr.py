#!/usr/bin/env python3
"""Genera la gramática ANTLR que consume el fuzzer, desde la gramática del proyecto.

    python3 herramientas/gramatica_a_antlr.py gramatica/minijava-ll1-final.g \
            > herramientas/fuzz/MiniJava.g4

La usa Grammarinator para dos cosas: derivar fuentes MiniJava al azar y volver a
parsearlos en árboles que después se mutan. La parte sintáctica sale del `.g`, que
es la fuente de verdad; la léxica, de herramientas/lexico_minijava.py.
"""
import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gramaticas_externas as externas  # noqa: E402
from primeros import EPSILON, parsear  # noqa: E402


def parser_antlr(g):
    agrupadas = {}
    orden = []
    for izquierda, cuerpo in g.producciones:
        if izquierda not in agrupadas:
            agrupadas[izquierda] = []
            orden.append(izquierda)
        agrupadas[izquierda].append(cuerpo)

    salida = []
    for izquierda in orden:
        salida.append(externas.nombre_de_no_terminal(izquierda))
        for i, cuerpo in enumerate(agrupadas[izquierda]):
            simbolos = []
            for simbolo in cuerpo:
                if simbolo == EPSILON:
                    continue
                simbolos.append(externas.nombre_de_no_terminal(simbolo) if g.es_no_terminal(simbolo)
                                else externas.nombre_de_regla(simbolo))
            salida.append(("    : " if i == 0 else "    | ") + " ".join(simbolos))
        salida.append("    ;")
        salida.append("")
    return "\n".join(salida)


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("gramatica")
    args = ap.parse_args()

    g = parsear(Path(args.gramatica).read_text(encoding="utf-8"))
    print("// GENERADO por herramientas/gramatica_a_antlr.py desde")
    print(f"// {args.gramatica}. No editar a mano: se regenera.")
    print("grammar MiniJava;")
    print()
    print(parser_antlr(g))
    print(externas.lexer_antlr())


if __name__ == "__main__":
    main()
