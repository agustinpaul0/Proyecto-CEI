#!/usr/bin/env python3
"""Genera el LaTeX de la gramática y de la tabla de Primeros desde un .g.

Se genera y no se escribe a mano para que el informe no pueda divergir de lo
que el verificador comprueba. Usa la misma convención tipográfica que la
cátedra en «Reglas de Sintaxis de MiniJava»: no terminales en \\texttt, mediante
\\nt{}, y terminales en sans-serif negrita, mediante \\tl{}.

    python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g producciones
    python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g primeros
"""
import re
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


TITULO = "\x00"


def separar_ligaduras(texto):
    for par in ("--", "<<", ">>"):
        texto = texto.replace(par, par[0] + "{}" + par[1])
    return texto


def escapar(texto):
    salida = "".join(ESCAPES.get(c, c) for c in texto)
    # LaTeX liga `--` en un guion medio y `---` en uno largo. El operador `--`
    # de MiniJava tiene que salir con dos guiones. Lo mismo con `<<` y `>>`,
    # que en T1 ligan en las comillas angulares «»: el cierre de un genérico
    # anidado tiene que salir como dos mayores separados.
    return separar_ligaduras(salida)


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
    """Comentario del .g a prosa LaTeX: `x` -> \\texttt{x}, <X> -> \\nt{X},
    *x* -> \\emph{x} (la misma marca de énfasis que ya lee el generador de
    Markdown, para que el comentario se escriba una sola vez)."""
    salida, i = [], 0
    while i < len(texto):
        c = texto[i]
        if c == "`":
            fin = texto.find("`", i + 1)
            if fin != -1:
                salida.append(r"\texttt{%s}" % escapar(texto[i + 1:fin]))
                i = fin + 1
                continue
        if c == "*":
            fin = texto.find("*", i + 1)
            if fin > i + 1 and not texto[i + 1:fin].strip() != texto[i + 1:fin]:
                salida.append(r"\emph{%s}" % texto_comentario(texto[i + 1:fin], no_terminales))
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
    return separar_ligaduras("".join(salida))


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

    lineas = []
    for linea_cruda in texto.splitlines():
        sin_com, _, comentario = linea_cruda.partition("#")
        cuerpo, _, marca = sin_com.partition("@")
        lineas.append((cuerpo.strip(), marca.strip(), comentario.strip()))

    def es_filete(indice):
        return (0 <= indice < len(lineas)
                and bool(re.fullmatch(r"[-=]{3,}", lineas[indice][2])))

    for numero, (cuerpo, marca, comentario) in enumerate(lineas):
        if cuerpo:
            if tipo != "regla":
                cerrar()
                tipo = "regla"
            actual.append((cuerpo, marca))
        elif comentario:
            # Los filetes ASCII del .g (`----`, `====`) son separadores
            # visuales del archivo, no prosa: acá los reemplaza el blanco
            # entre párrafos. Un filete con un título adentro lo conserva,
            # pero como título en negrita, no pegado a la prosa que sigue.
            # Un título encerrado entre dos filetes es un banner de sección del
            # .g, y este informe ya tiene sus propias secciones: no va.
            if es_filete(numero - 1) and es_filete(numero + 1):
                continue
            filete = re.fullmatch(r"[-=]{3,}(.*?)[-=]{0,}", comentario)
            if filete:
                comentario = filete.group(1).strip()
            if not comentario:
                continue
            if filete and not comentario.isupper():
                cerrar()
                tipo = "comentario"
                actual.append(TITULO + comentario)
                continue
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
            titulo = ""
            if contenido and contenido[0].startswith(TITULO):
                titulo = r"\textbf{%s.} " % texto_comentario(
                    contenido[0][len(TITULO):].rstrip("."), nts)
                contenido = contenido[1:]
            prosa = texto_comentario(" ".join(contenido), nts)
            print(r"\noindent{\small %s%s\par}" % (titulo, prosa))
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
                etiqueta = r"\marca{%s}{%s}" % (marca[0], escapar(marca[1:]))
            print(r"\regla{%s}{%s}%s" % (izq.strip(), derecha, etiqueta))
        print(r"\end{producciones}")
        print()


def tabla_primeros(ruta):
    _, g, _ = leer(ruta)
    vaciables, primeros_nt, _ = analizar(g)
    nts = g.no_terminales
    # Dos columnas angostas no alcanzan: un no terminal como
    # <InicializacionDeDeclarador> no se puede partir y desborda sobre la
    # columna de al lado. Se usa una sola columna a lo ancho de la caja, con la
    # producción en un renglón y sus Primeros sangrados debajo.
    print(r"\begingroup\small")
    print(r"\setlength{\parindent}{0pt}")
    print(r"\setlength{\parskip}{0.7ex}")
    print(r"\raggedright")
    print(r"\hyphenpenalty=10000 \exhyphenpenalty=10000 \hbadness=10000")
    for nt in nts:
        for cuerpo in g.por_izquierda[nt]:
            conjunto = primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo)
            derecha = " ".join(render(s, nts) for s in cuerpo)
            items = sorted(c for c in conjunto if c != EPSILON)
            texto = ", ".join(r"\tl{%s}" % escapar(c) for c in items)
            if EPSILON in conjunto:
                texto = (texto + ", " if texto else "") + r"\eps"
            print(r"\par\hangindent=2.2em\hangafter=1")
            print(r"\regla{%s}{%s}\nobreak\par" % (nt, derecha))
            # Los Primeros van en su propio bloque sangrado, para que al cortarse
            # la continuación no vuelva al margen y se confunda con una producción.
            print(r"{\leftskip=1.6em\relax\noindent\textup{Primeros} $=$ \{ %s \}\par}"
                  % texto)
    print(r"\endgroup")


if __name__ == "__main__":
    ruta, modo = sys.argv[1], sys.argv[2]
    {"producciones": producciones, "primeros": tabla_primeros}[modo](ruta)
