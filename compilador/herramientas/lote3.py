#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lote 3: numeros, identificadores, palabras clave, operadores, estructura."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generar_casos import sin_errores, con_errores, E, T

# ------------------------------------------------------------------ numeros
# '1.e5' es un floatLiteral valido: EstadoFloatConPunto acepta el exponente
# justo despues del punto, con parte fraccionaria vacia. Contraintuitivo pero
# es lo que hace Java.
sin_errores("floatsContraintuitivos.java", [
    "1.e5",
    "1.E-5",
    "1e5e5",
    "1.2.3",
    "1.5f5",
    "1ff",
], [("1.e5", 1), ("1.E-5", 2),
    ("1e5", 3), ("e5", 3),
    ("1.2", 4), (".", 4), ("3", 4),
    ("1.5f", 5), ("5", 5),
    ("1f", 6), ("f", 6)])

# El limite es de NUEVE DIGITOS, no de valor: por eso 1234567890.5 es un float
# valido aunque su parte entera tenga diez digitos (la validacion de
# EstadoEntero solo corre si la corrida NO continua hacia un float).
sin_errores("enteroLargoDentroDeUnFloat.java", [
    "1234567890.5",
    "1234567890e5",
    "1234567890f",
    "999999999",
    "000000000",
], [("1234567890.5", 1), ("1234567890e5", 2), ("1234567890f", 3),
    ("999999999", 4), ("000000000", 5)])

sin_errores("exponentesLargos.java", [
    "1e123456",
    "1E000000009",
    "9e0f",
    "0e0D",
], [("1e123456", 1), ("1E000000009", 2), ("9e0f", 3), ("0e0D", 4)])

# Un float SIEMPRE empieza con digito (Pautas Etapa 1), asi que '.5' son dos
# tokens: punto y entero.
sin_errores("puntoSeguidoDeDigito.java", [
    ".5",
    "..5",
    "obj.campo",
], [(".", 1), ("5", 1), (".", 2), (".", 2), ("5", 2),
    ("obj", 3), (".", 3), ("campo", 3)])

# ----------------------------------------------------------- identificadores
sin_errores("identificadoresConGuionBajo.java", [
    "a_",
    "a__b",
    "a_1",
    "A_",
    "A_1_b",
], [("a_", 1), ("a__b", 2), ("a_1", 3), ("A_", 4), ("A_1_b", 5)])

sin_errores("idGenTodasLasLetras.java",
            ["A B C D E F G H I J K L M N O P Q R S T U V W X Y Z"],
            [(chr(c), 1) for c in range(ord("A"), ord("Z") + 1)])

sin_errores("idClaseMinimos.java", [
    "Ab A1 A_",
    "Zz Z9 Z_",
], [("Ab", 1), ("A1", 1), ("A_", 1), ("Zz", 2), ("Z9", 2), ("Z_", 2)])

# 'double' NO es palabra reservada en MiniJava: solo hay un tipo de punto
# flotante, 'float'. Los sufijos d/D del literal son otra cosa.
sin_errores("doubleEsUnIdentificadorComun.java", [
    "double x",
    "double = 1d",
], [("double", 1), ("x", 1), ("double", 2), ("=", 2), ("1d", 2)])

sin_errores("casiPalabrasClave.java", [
    "clas classs iff elsee",
    "nul truee falsee floatt",
    "Class If While Return",
], [("clas", 1), ("classs", 1), ("iff", 1), ("elsee", 1),
    ("nul", 2), ("truee", 2), ("falsee", 2), ("floatt", 2),
    ("Class", 3), ("If", 3), ("While", 3), ("Return", 3)])

sin_errores("identificadorMuyLargo.java",
            ["a" + "b" * 999, "C" + "d" * 999],
            [("a" + "b" * 999, 1), ("C" + "d" * 999, 2)])

# ------------------------------------------------------ operadores y puntuacion
sin_errores("operadoresPegadosSinEspacios.java", [
    ">=<=!===",
    "&&||",
    "++--",
    "+-*/%",
], [(">=", 1), ("<=", 1), ("!=", 1), ("==", 1),
    ("&&", 2), ("||", 2),
    ("++", 3), ("--", 3),
    ("+", 4), ("-", 4), ("*", 4), ("/", 4), ("%", 4)])

# Estos NO son operadores de MiniJava: se parten en dos tokens cada uno.
sin_errores("secuenciasQueNoSonUnOperador.java", [
    "a += b",
    "a -= b",
    "a *= b",
    "a /= b",
    "a %= b",
    "a -> b",
], [("a", 1), ("+", 1), ("=", 1), ("b", 1),
    ("a", 2), ("-", 2), ("=", 2), ("b", 2),
    ("a", 3), ("*", 3), ("=", 3), ("b", 3),
    ("a", 4), ("/", 4), ("=", 4), ("b", 4),
    ("a", 5), ("%", 5), ("=", 5), ("b", 5),
    ("a", 6), ("-", 6), (">", 6), ("b", 6)])

sin_errores("dosPuntosYPuntosRepetidos.java", [
    "::",
    "...",
    ";;",
    ",,",
], [(":", 1), (":", 1), (".", 2), (".", 2), (".", 2),
    (";", 3), (";", 3), (",", 4), (",", 4)])

# --------------------------------------------------------------- estructura
sin_errores("programaConGenericos.java", [
    "class Caja<T> implements Contenedor<T> {",
    "    private T valor;",
    "    public T obtener() { return this.valor; }",
    "}",
], [("class", 1), ("Caja", 1), ("<", 1), ("T", 1), (">", 1),
    ("implements", 1), ("Contenedor", 1), ("<", 1), ("T", 1), (">", 1), ("{", 1),
    ("private", 2), ("T", 2), ("valor", 2), (";", 2),
    ("public", 3), ("T", 3), ("obtener", 3), ("(", 3), (")", 3), ("{", 3),
    ("return", 3), ("this", 3), (".", 3), ("valor", 3), (";", 3), ("}", 3),
    ("}", 4)])

sin_errores("lineaMuyLarga.java",
            [" ".join(["v%d" % i for i in range(200)])],
            [("v%d" % i, 1) for i in range(200)])

sin_errores("archivoSinSaltoFinal.java", ["v1 v2 v3"],
            [("v1", 1), ("v2", 1), ("v3", 1)], salto_final=False)
