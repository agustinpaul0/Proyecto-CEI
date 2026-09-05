#!/usr/bin/env python3
"""Genera el LaTeX de la gramática y de la tabla de Primeros desde un .g.

Se genera y no se escribe a mano para que el informe no pueda divergir de lo
que el verificador comprueba. Usa la misma convención tipográfica que la
cátedra en «Reglas de Sintaxis de MiniJava»: no terminales en \\texttt, mediante
\\nt{}, y terminales en sans-serif negrita, mediante \\tl{}.

    python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g producciones
    python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g primeros
"""
import sys

sys.path.insert(0, "herramientas")
from primeros import (  # noqa: E402
    EPSILON,
    analizar,
    longitudes_derivables,
    parsear,
    primeros_de_secuencia,
)

ESCAPES = {
    "{": r"\{", "}": r"\}", "$": r"\$", "&": r"\&", "%": r"\%",
    "#": r"\#", "_": r"\_", "^": r"\^{}", "~": r"\~{}",
}


def escapar(texto):
    salida = "".join(ESCAPES.get(c, c) for c in texto)
    # LaTeX liga `--` en un guion medio y `---` en uno largo. El operador `--`
    # de MiniJava tiene que salir con dos guiones.
    return salida.replace("--", "-{}-")


def render(simbolo, no_terminales):
    if simbolo == EPSILON:
        return r"\eps"
    if simbolo in no_terminales:
        return r"\nt{%s}" % simbolo
    return r"\tl{%s}" % escapar(simbolo)


def leer(ruta):
    texto = open(ruta, encoding="utf-8").read()
    g = parsear(texto)
    marcas = {}
    for linea in texto.splitlines():
        cuerpo, _, marca = linea.split("#", 1)[0].partition("@")
        if marca.strip() and "->" in cuerpo:
            marcas[cuerpo.split("->", 1)[0].strip()] = marca.strip()
    return texto, g, marcas


def texto_comentario(texto, no_terminales):
    """Comentario del .g a prosa LaTeX: `x` -> \\texttt{x}, <X> -> \\nt{X}."""
    salida, i = [], 0
    while i < len(texto):
        c = texto[i]
        if c == "`":
            fin = texto.find("`", i + 1)
            if fin != -1:
                salida.append(r"\texttt{%s}" % escapar(texto[i + 1:fin]))
                i = fin + 1
                continue
        if c == "<":
            fin = texto.find(">", i + 1)
            if fin != -1 and texto[i + 1:fin] in no_terminales:
                salida.append(r"\nt{%s}" % texto[i + 1:fin])
                i = fin + 1
                continue
        salida.append(ESCAPES.get(c, c))
        i += 1
    return "".join(salida)


def producciones(ruta):
    texto, g, _ = leer(ruta)
    nts = g.no_terminales
    # Se agrupan las líneas en bloques: o una tanda de producciones, o un
    # párrafo de comentario. Así los comentarios de varias líneas del .g no
    # salen como párrafos sueltos.
    bloques, actual, tipo = [], [], None

    def cerrar():
        if actual:
            bloques.append((tipo, list(actual)))
            actual.clear()

    for linea_cruda in texto.splitlines():
        sin_com, _, comentario = linea_cruda.partition("#")
        cuerpo, _, marca = sin_com.partition("@")
        cuerpo, marca, comentario = cuerpo.strip(), marca.strip(), comentario.strip()
        if cuerpo:
            if tipo != "regla":
                cerrar()
                tipo = "regla"
            actual.append((cuerpo, marca))
        elif comentario and not set(comentario) <= set("= "):
            # Los banners en MAYÚSCULAS son la estructura del .g, no prosa:
            # este informe ya tiene sus propias secciones.
            if comentario.isupper():
                continue
            if tipo != "comentario":
                cerrar()
                tipo = "comentario"
            actual.append(comentario)
    cerrar()

    # La cabecera del .g documenta el formato del archivo y lista los logros:
    # este informe ya explica las dos cosas en sus propias secciones.
    primera = next((i for i, (t, _) in enumerate(bloques) if t == "regla"), 0)
    bloques = bloques[primera:]

    for clase, contenido in bloques:
        if clase == "comentario":
            prosa = texto_comentario(" ".join(contenido), nts)
            print(r"\noindent{\small\color{grisMedio}%s\par}" % prosa)
            print()
            continue
        print(r"\begin{producciones}")
        for cuerpo, marca in contenido:
            izq, der = cuerpo.split("->", 1)
            alternativas, act = [], []
            for token in der.split():
                if token == "|":
                    alternativas.append(act)
                    act = []
                else:
                    act.append(token)
            alternativas.append(act)
            derecha = r" \alt{} ".join(
                " ".join(render(s, nts) for s in alt) for alt in alternativas
            )
            etiqueta = ""
            if marca:
                etiqueta = (r"\hfill\mbox{\footnotesize\color{grisMedio}"
                            r"[%s\,\texttt{%s}]}" % (marca[0], escapar(marca[1:])))
            print(r"\regla{%s}{%s}%s" % (izq.strip(), derecha, etiqueta))
        print(r"\end{producciones}")
        print()


def tabla_primeros(ruta):
    _, g, _ = leer(ruta)
    vaciables, primeros_nt, _ = analizar(g)
    nts = g.no_terminales
    print(r"\begin{longtable}{@{}p{0.30\textwidth}p{0.63\textwidth}@{}}")
    print(r"\toprule")
    print(r"\textbf{Producción} & \textbf{Primeros} \\")
    print(r"\midrule\endhead")
    for nt in nts:
        for cuerpo in g.por_izquierda[nt]:
            conjunto = primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo)
            derecha = " ".join(render(s, nts) for s in cuerpo)
            items = sorted(c for c in conjunto if c != EPSILON)
            texto = ", ".join(r"\tl{%s}" % escapar(c) for c in items)
            if EPSILON in conjunto:
                texto = (texto + ", " if texto else "") + r"\eps"
            print(r"\regla{%s}{%s} & \{ %s \} \\" % (nt, derecha, texto))
    print(r"\bottomrule")
    print(r"\end{longtable}")


if __name__ == "__main__":
    ruta, modo = sys.argv[1], sys.argv[2]
    {"producciones": producciones, "primeros": tabla_primeros}[modo](ruta)
