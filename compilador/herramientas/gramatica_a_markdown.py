#!/usr/bin/env python3
"""Vuelca una gramática .g a la notación BNF-extendida del PDF de la cátedra.

Se genera desde el .g y no se escribe a mano para que el documento y lo que el
verificador comprueba no puedan divergir.

    python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-logros.g
"""
import sys

TERMINALES_PALABRA = {
    "class", "interface", "extends", "implements", "static", "void", "public",
    "private", "boolean", "char", "int", "float", "var", "return", "if", "else",
    "while", "for", "this", "new", "null", "true", "false", "eof",
    "idClase", "idGen", "idMetVar", "intLiteral", "charLiteral", "floatLiteral",
    "stringLiteral",
}


def main():
    ruta = sys.argv[1]
    saltar_cabecera = "--sin-cabecera" in sys.argv
    texto = open(ruta, encoding="utf-8").read()
    no_terminales = set()
    for linea in texto.splitlines():
        linea = linea.split("#", 1)[0].split("@", 1)[0].strip()
        if "->" in linea:
            no_terminales.add(linea.split("->", 1)[0].strip())

    def render(simbolo):
        if simbolo == "EPSILON":
            return "ε"
        if simbolo in no_terminales:
            return f"<{simbolo}>"
        if simbolo in TERMINALES_PALABRA:
            return f"**{simbolo}**"
        return f"`{simbolo}`"

    salida = []
    for linea_cruda in texto.splitlines():
        sin_comentario, _, comentario = linea_cruda.partition("#")
        cuerpo, _, marca = sin_comentario.partition("@")
        cuerpo, marca = cuerpo.strip(), marca.strip()
        if not cuerpo:
            salida.append(("comentario", comentario.strip()))
            continue
        izquierda, derecha = cuerpo.split("->", 1)
        alternativas, actual = [], []
        for token in derecha.split():
            if token == "|":
                alternativas.append(actual)
                actual = []
            else:
                actual.append(token)
        alternativas.append(actual)
        texto_der = " | ".join(" ".join(render(s) for s in alt) for alt in alternativas)
        etiqueta = ""
        if marca:
            signo = "sólo con" if marca[0] == "+" else "sólo sin"
            etiqueta = f"  *[{signo} `{marca[1:]}`]*"
        salida.append(("regla", f"<{izquierda.strip()}> ::= {texto_der}{etiqueta}"))

    # La cabecera del .g explica el formato del archivo, no la gramática.
    if saltar_cabecera:
        primera = next(i for i, (t, _) in enumerate(salida) if t == "regla")
        salida = salida[primera:]

    anterior = None
    for tipo, contenido in salida:
        if tipo == "comentario":
            if contenido.startswith("===="):
                continue
            if not contenido:
                if anterior != "vacio":
                    print()
                anterior = "vacio"
                continue
            if anterior == "regla":
                print()
            print(f"> {contenido}")
            anterior = "comentario"
        else:
            if anterior == "comentario":
                print()
            print(f"{contenido}  ")
            anterior = "regla"


if __name__ == "__main__":
    main()
