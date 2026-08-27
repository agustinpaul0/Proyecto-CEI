#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lote 2: strings, escapes unicode, caracteres desconocidos, comentarios."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generar_casos import sin_errores, con_errores, E, T

# --------------------------------------------------------------- litString
sin_errores("stringVacioYEscapesDeJava.java", [
    '""',
    '"a\\"b"',
    '"\\\\"',
    '"\\b\\s\\t\\n\\f\\r\\"\\\'\\\\"',
    '"\\0 \\7 \\77"',
], [('""', 1), ('"a\\"b"', 2), ('"\\\\"', 3),
    ('"\\b\\s\\t\\n\\f\\r\\"\\\'\\\\"', 4), ('"\\0 \\7 \\77"', 5)])

# Simbolos que FUERA de un string son error lexico, adentro son contenido.
sin_errores("stringConSimbolosNoLexicos.java", [
    '"# @ ? ~ ^ $ ` |"',
    '"/* no abre comentario */"',
    '"// tampoco"',
], [('"# @ ? ~ ^ $ ` |"', 1), ('"/* no abre comentario */"', 2), ('"// tampoco"', 3)])

# MiniJava NO hace la pre-traduccion unicode de Java (JLS 3.3): \\u0022 queda
# como seis caracteres del lexema y NO cierra el string. En Java esta misma
# linea no compila.
sin_errores("stringConEscapeUnicodeDeComilla.java", [
    '"a\\u0022b"',
    '"\\u0041\\uu0041\\uuuu0041"',
    '"\\u00ff \\u00FF \\u00fF"',
], [('"a\\u0022b"', 1), ('"\\u0041\\uu0041\\uuuu0041"', 2), ('"\\u00ff \\u00FF \\u00fF"', 3)])

sin_errores("charConComillaDobleYComa.java", [
    "','",
    "'\"'",
    "':'",
    "';'",
], [("','", 1), ("'\"'", 2), ("':'", 3), ("';'", 4)])

# ------------------------------------------------------- escapes invalidos
# Un escape mal formado descarta el litString hasta su comilla de cierre: se
# reporta el primero y no se emite token. Por eso el segundo escape invalido
# de un mismo literal no aparece, y los tokens posteriores a la comilla de
# cierre si aparecen.
con_errores("stringConEscapeInvalido.java",
            [E('"hola \\q', 1)],
            ['"hola \\q mundo"'])

con_errores("stringConDosEscapesInvalidos.java",
            [E('"a\\q', 1)],
            ['"a\\q b\\z c"'])

con_errores("stringConEscapeOctalFueraDeRango.java",
            [E('"a\\8', 1)],
            ['"a\\8"'])

con_errores("stringConEscapeInvalidoYLuegoCodigoValido.java",
            [E('"x\\q', 1), T("+", 1), T("v1", 1)],
            ['"x\\q" + v1'])

# ----------------------------------------------------- unicode mal formado
# OJO: 'backslash-u-0-4-1-f' seria VALIDO, porque la f es un digito
# hexadecimal. Para cortar en el cuarto digito hace falta un caracter que no
# lo sea.
con_errores("escapeUnicodeConCuartoDigitoInvalido.java",
            [E("'\\u041", 1), T("z", 1), E("'", 1)],
            ["'\\u041z'"])

con_errores("escapeUnicodeConEspacio.java",
            [E("'\\u", 1), T("041", 1), E("'", 1)],
            ["'\\u 041'"])

# Dentro de un litString el unicode mal formado sigue la misma regla que
# cualquier otro escape mal formado: descarta hasta la comilla de cierre. En
# los charLiteral de arriba no, ahi la recuperacion sigue siendo estructural.
con_errores("escapeUnicodeEnStringConDigitoInvalido.java",
            [E('"a\\u12', 1)],
            ['"a\\u12G4"'])

con_errores("escapeUnicodeConMarcadorRepetidoYDigitoInvalido.java",
            [E("'\\uu12", 1), T("G4", 1), E("'", 1)],
            ["'\\uu12G4'"])

# ------------------------------------------------- caracteres desconocidos
con_errores("virgulilla.java", [T("v1", 1), E("~", 1), T("v2", 1)], ["v1 ~ v2"])
con_errores("circunflejo.java", [T("v1", 1), E("^", 1), T("v2", 1)], ["v1 ^ v2"])
con_errores("dolarSuelto.java", [T("v1", 1), E("$", 1), T("v2", 1)], ["v1 $ v2"])
con_errores("guionBajoSuelto.java", [T("v1", 1), E("_", 1), T("v2", 1)], ["v1 _ v2"])

# En Java _x es un identificador valido; en MiniJava un idMV empieza con
# minuscula, asi que el '_' es un caracter desconocido y 'x' arranca un idMV.
con_errores("identificadorQueEmpiezaConGuionBajo.java",
            [E("_", 1), T("x", 1)],
            ["_x"])

# Una letra que no es ni mayuscula ni minuscula (Character.isLowerCase y
# isUpperCase son false) no puede EMPEZAR un identificador, aunque
# Character.isLetterOrDigit si la acepta para continuarlo: ver el caso
# sinErrores/identificadorConLetraSinCaja.java.
con_errores("letraSinCajaAlEmpezarIdentificador.java",
            [E("中", 1), T("abc", 1)],
            ["中abc"])

sin_errores("identificadorConLetraSinCaja.java", [
    "a中",
    "b中c",
], [("a中", 1), ("b中c", 2)])

con_errores("veinteCaracteresDesconocidosSeguidos.java",
            [E("@", 1)] * 20,
            ["@" * 20])

# --------------------------------------------------- comentarios sin cerrar
con_errores("comentarioSinCerrarVacio.java",
            [T("v1", 1), E("/*", 2)],
            ["v1", "/*"], salto_final=False)

con_errores("comentarioSinCerrarSoloAsteriscos.java",
            [T("v1", 1), E("/*", 2)],
            ["v1", "/***"], salto_final=False)

# /*/ no cierra: EstadoBarra descarta el '*' de apertura y arranca con
# ultimoFueAsterisco en false, asi que la '/' siguiente no cierra nada.
con_errores("comentarioBarraAsteriscoBarraNoCierra.java",
            [T("v1", 1), E("/*", 2)],
            ["v1", "/*/"], salto_final=False)

con_errores("comentarioSinCerrarConCierreDentroDeStringPrevio.java",
            [T('"*/"', 1), E("/*", 2)],
            ['"*/"', "/* no cierra"], salto_final=False)
