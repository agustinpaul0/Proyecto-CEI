#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lote 1: comentarios, escapes unicode fuera de literal, y los casos que
fijan el criterio de recuperacion (ningun caracter tragado)."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generar_casos import sin_errores, con_errores, E, T

# ---------------------------------------------------------------- comentarios
sin_errores("comentarioAsteriscosMultiples.java", [
    "/**/ a",
    "/***/ b",
    "/****/ c",
    "/* x **/ d",
    "/* x ***/ e",
    "/** doc **/ f",
], [("a", 1), ("b", 2), ("c", 3), ("d", 4), ("e", 5), ("f", 6)])

sin_errores("comentarioConBarrasInternas.java", [
    "/* // no abre nada */ a",
    "// /* tampoco abre nada",
    "/* / */ b",
    "/*/ no cierra aca */ c",
], [("a", 1), ("b", 3), ("c", 4)])

sin_errores("comentarioPegadoATokens.java", [
    "a/*x*/b",
    "1/**/2",
    "c/**/+/**/d",
    "e//sin espacio",
], [("a", 1), ("b", 1), ("1", 2), ("2", 2),
    ("c", 3), ("+", 3), ("d", 3), ("e", 4)])

# El escape unicode fuera de un literal: divergencia con Java documentada en
# el informe. Java traduce \\uXXXX en todo el archivo antes de tokenizar (JLS
# 3.3), asi que estas lineas serian errores de compilacion en Java; MiniJava
# solo interpreta el escape dentro de un charLiteral o un litString.
sin_errores("comentarioConEscapeUnicode.java", [
    "// escape mal formado que en Java no compila: \\u12G4",
    "// un salto de linea escapado no corta el comentario: \\u000A esto sigue comentado",
    "/* ni cierra el comentario multilinea: \\u002A\\u002F sigue adentro */ a",
    "// marcador repetido \\uu0041 y suelto \\u al final",
], [("a", 3)])

# ------------------------------------------- el criterio: nada se traga nunca
con_errores("ampersandPegadoAlIdentificador.java",
            [T("v1", 1), E("&", 1), T("v2", 1)],
            ["v1 &v2"])

con_errores("pipeSuelto.java",
            [T("v1", 1), E("|", 1), T("v2", 1)],
            ["v1 | v2"])

con_errores("pipePegadoAlIdentificador.java",
            [T("v1", 1), E("|", 1), T("v2", 1)],
            ["v1 |v2"])

con_errores("ampersandTriple.java",
            [T("a", 1), T("&&", 1), E("&", 1), T("b", 1)],
            ["a &&& b"])

con_errores("enteroLargoPegadoAIdentificador.java",
            [E("1234567890", 1), T("abc", 1)],
            ["1234567890abc"])

con_errores("enteroLargoSeguidoDeOperador.java",
            [E("1234567890", 1), T("+", 1), T("1", 1)],
            ["1234567890+1"])

con_errores("enteroLargoSeguidoDePuntoYComa.java",
            [E("1234567890", 1), T(";", 1)],
            ["1234567890;"])

con_errores("exponenteSinDigitosSeguidoDePuntoYComa.java",
            [E("1e", 1), T(";", 1)],
            ["1e;"])

con_errores("exponenteSinDigitosSeguidoDeParentesis.java",
            [E("1e", 1), T(")", 1)],
            ["1e)"])

con_errores("exponenteSeguidoDeLetra.java",
            [E("1e", 1), T("X", 1)],
            ["1eX"])

con_errores("exponenteConSignoSeguidoDePuntoYComa.java",
            [E("1.5e+", 1), T(";", 1)],
            ["1.5e+;"])

con_errores("enteroDeVeinteDigitos.java",
            [E("12345678901234567890", 1), T(";", 1)],
            ["12345678901234567890;"])

# ------------------------------------------------------ char mal formado
con_errores("charConTresCaracteres.java",
            [E("'a", 1), T("bbc", 1), E("'", 1)],
            ["'abbc'"])

# La comilla de cierre huerfana abre un char literal nuevo que se come el
# espacio siguiente como contenido valido (' ' es un charLiteral legitimo), y
# recien falla al ver el '+'. De ahi que el lexema del segundo error sea
# comilla-mas-espacio, no la comilla sola.
con_errores("charMalFormadoSeguidoDeCodigoValido.java",
            [E("'a", 1), T("b", 1), E("' ", 1), T("+", 1), T("1", 1)],
            ["'ab' + 1"])

con_errores("tresComillasSimples.java",
            [E("''", 1), E("'", 1)],
            ["'''"])
