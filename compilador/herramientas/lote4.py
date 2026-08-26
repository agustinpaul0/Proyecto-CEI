#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lote 4: caracteres de control, Unicode raro, fin de linea y cascadas."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generar_casos import sin_errores, con_errores, E, T

NUL = chr(0)
SOH = chr(1)
VT = chr(11)
FF = chr(12)
SUB = chr(26)          # el centinela END_OF_FILE de la catedra
BOM = "﻿"
NBSP = " "

# ------------------------------------------- caracteres de control y blancos
# MiniJava solo considera blancos el espacio, el tabulador y el salto de
# linea. Java ademas acepta form feed; aca es un error lexico, y su lexema
# tiene que salir ESCAPADO para no romper el formato de una linea por
# elemento.
con_errores("formFeedYTabuladorVertical.java",
            [T("v1", 1), E("\\f", 1), E("\\u000b", 1), T("v2", 1)],
            ["v1 " + FF + " " + VT + " v2"])

con_errores("caracteresDeControlNulos.java",
            [T("v1", 1), E("\\u0000", 1), E("\\u0001", 1), T("v2", 1)],
            ["v1 " + NUL + " " + SOH + " v2"])

# El centinela de fin de archivo de la catedra es (char) 26. Antes de
# distinguir el EOF por posicion, este byte truncaba el analisis en silencio y
# el compilador informaba [SinErrores].
con_errores("centinelaEofEmbebido.java",
            [T("v1", 1), E("\\u001a", 1), T("v2", 2)],
            ["v1" + SUB, "v2"])

# El BOM no se descarta (javac si lo salta): es un caracter desconocido mas.
con_errores("marcaDeOrdenDeBytes.java",
            [T("v1", 1), E(BOM, 1), T("v2", 1)],
            ["v1 " + BOM + " v2"])

con_errores("espacioNoRompible.java",
            [T("v1", 1), E(NBSP, 1), T("v2", 1)],
            ["v1 " + NBSP + " v2"])

# -------------------------------------------------------------- Unicode raro
# Character.isDigit acepta los digitos arabe-indicos, asi que arrancan un
# intLiteral y cuentan para el limite de nueve digitos. Java NO los admite en
# un literal numerico: divergencia real, fijada aca a proposito.
sin_errores("digitosArabeIndicos.java", [
    "١٢٣",
    "٩٩٩٩٩٩٩٩٩",
], [("١٢٣", 1), ("٩٩٩٩٩٩٩٩٩", 2)])

con_errores("digitosArabeIndicosDeDiez.java",
            [E("١٢٣٤٥٦٧٨٩٠", 1)],
            ["١٢٣٤٥٦٧٨٩٠"])

# Character.digit(c, 16) tambien acepta digitos no ASCII, asi que este escape
# unicode se da por valido aunque la ER documentada diga [0-9a-fA-F].
sin_errores("escapeUnicodeConDigitosNoAscii.java", [
    "'\\u٠٠٤١'",
], [("'\\u٠٠٤١'", 1)])

sin_errores("literalesConUnicodeCrudo.java", [
    '"café con ñoño y 中文 y ру́сский"',
    "'ñ'",
    "'中'",
    "'ا'",
], [('"café con ñoño y 中文 y ру́сский"', 1), ("'ñ'", 2), ("'中'", 3), ("'ا'", 4)])

# ------------------------------------------------------------- fin de linea
sin_errores("finDeLineaMezclado.java", [
    "v1\r",
    "v2",
    "v3\r",
], [("v1", 1), ("v2", 2), ("v3", 3)])

# ------------------------------------------------- char y string sin cerrar
# Cascada completa de "'abc' d 'e": el primer error corta en la 'b', la
# comilla de cierre huerfana abre un char que se come el espacio y muere en la
# 'd', y la ultima comilla abre un tercer char que muere en el fin de linea.
con_errores("charSinCerrarConOtraComillaEnLaLinea.java",
            [E("'a", 1), T("bc", 1), E("' ", 1), T("d", 1), E("'e", 1)],
            ["'abc' d 'e"])

con_errores("charConBarraInvertidaYSaltoDeLinea.java",
            [E("'\\", 1), T("x", 2)],
            ["'\\", "x"])

con_errores("stringConBarraInvertidaYSaltoDeLinea.java",
            [E('"abc\\', 1), T("x", 2)],
            ['"abc\\', "x"])

con_errores("comillaDobleTriple.java",
            [T('""', 1), E('"', 1)],
            ['"""'])

sin_errores("stringConTabuladorInterior.java", [
    '"antes\tdespues"',
], [('"antes\tdespues"', 1)])

# ------------------------------------------------------------------ cascadas
con_errores("errorEntreLiteralesValidos.java",
            [T("V1", 1), T("+", 1), E("#", 1), T('"hola"', 1), E("#", 1)],
            ['V1 + # "hola" #'])

con_errores("erroresEnLasTresPrimerasLineas.java",
            [E("@", 1), E("#", 2), E("?", 3), T("v1", 3)],
            ["@", "#", "? v1"])

con_errores("errorAntesYDespuesDeComentario.java",
            [E("@", 1), T("v1", 1), E("#", 1), T("v2", 2)],
            ["@ /* c */ v1 # /* d */", "v2"])

con_errores("errorEnLaUltimaColumnaSinSaltoFinal.java",
            [T("v1", 1), E("@", 1)],
            ["v1 @"], salto_final=False)
