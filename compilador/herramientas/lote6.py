#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lote 6: notaciones numericas de Java que MiniJava no tiene.

La ER de la catedra para intLiteral es Digito{1,9}: decimal y nada mas. No hay
octal, ni hexadecimal, ni guiones bajos separadores. Verificado con javac:

    int a = 041;    -> compila (octal, vale 33)
    int b = 09;     -> error: illegal digit in an octal literal
    int c = 0x1F;   -> compila (hexadecimal)
    int d = 1_000;  -> compila (separadores desde Java 7)

En MiniJava los cuatro se leen con la regla decimal simple, asi que ninguno es
error: 041 y 09 son intLiteral de tres y dos digitos, y 0x1F y 1_000 se parten
en varios tokens por maximal munch.
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generar_casos import sin_errores, con_errores, E, T

sin_errores("notacionesNumericasDeJava.java", [
    "041",
    "09",
    "0x1F",
    "0b1010",
], [("041", 1),
    ("09", 2),
    ("0", 3), ("x1F", 3),
    ("0", 4), ("b1010", 4)])

# El separador de miles es el unico de los cuatro que ademas da error: el '_'
# no puede continuar un intLiteral (no es digito) ni empezar un identificador
# (un idMV arranca con minuscula), asi que queda como caracter desconocido.
con_errores("separadorDeMilesDeJava.java",
            [T("1", 1), E("_", 1), T("000", 1)],
            ["1_000"])
