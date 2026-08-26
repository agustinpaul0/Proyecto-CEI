#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lote 5: limites de los literales numericos.

La catedra acota SOLO el intLiteral (1 a 9 digitos). La ER del floatLiteral
usa Digito+ sin cota en las tres partes, y Pautas Etapa 1 habla de "la misma
estructura que los floats de Java", o sea de la forma, no del rango.

Java tampoco limita la cantidad de digitos de un float: lo que rechaza es un
literal no nulo que se redondee a infinito o a cero, y eso es un chequeo de
VALOR, no lexico. Verificado con javac:

    float a = 1.0000...0001f;   (60+ digitos)  -> compila
    float c = 3.4e38f;                         -> compila
    float d = 3.5e38f;                         -> error: floating-point number too large
    float g = 1e-46f;                          -> error: floating-point number too small

MiniJava no hace ninguno de los dos chequeos, y estos casos lo fijan.
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generar_casos import sin_errores, con_errores, E, T

largo = "1." + "0" * 60 + "1"
sin_errores("floatSinLimiteDeDigitos.java", [
    largo,
    "1" * 40 + ".5",
    "0." + "0" * 50 + "1",
    "1e" + "9" * 20,
], [(largo, 1), ("1" * 40 + ".5", 2), ("0." + "0" * 50 + "1", 3), ("1e" + "9" * 20, 4)])

# Los cuatro desbordan el rango de float o de double y en Java NO compilan.
# En MiniJava son floatLiteral validos: el chequeo de rango es semantico y la
# catedra solo pidio "la misma estructura".
sin_errores("floatFueraDelRangoDeJava.java", [
    "3.5e38f",
    "1e-46f",
    "1.8e308",
    "1e999",
], [("3.5e38f", 1), ("1e-46f", 2), ("1.8e308", 3), ("1e999", 4)])

# La asimetria fina: el limite de nueve digitos solo se aplica cuando la
# corrida NO continua hacia un float.
sin_errores("nueveDigitosEsElLimiteExacto.java", [
    "999999999",
    "999999999.0",
    "9999999999.0",
    "9999999999e1",
    "9999999999f",
], [("999999999", 1), ("999999999.0", 2), ("9999999999.0", 3),
    ("9999999999e1", 4), ("9999999999f", 5)])

con_errores("diezDigitosSinContinuacionDeFloat.java",
            [E("9999999999", 1), T(";", 1)],
            ["9999999999;"])

# El limite de MiniJava es de DIGITOS, el de Java es de VALOR: por eso este
# entero, que vale 1, es un error aca y es valido en Java.
con_errores("diezCerosConValorUno.java",
            [E("0000000001", 1), T(";", 1)],
            ["0000000001;"])
