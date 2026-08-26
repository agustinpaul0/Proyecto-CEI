#!/usr/bin/env python3
"""Genera casos de resources/sinErrores y resources/conErrores.

sinErrores: fuente + linea en blanco + expectativas //#lexema,linea. La linea
del EOF se calcula sola: es "saltos de linea consumidos + 1", y como las
lineas //# son lineas fisicas del archivo, cuentan.

conErrores: la cabecera son lineas de comentario al principio, y desplazan el
fuente hacia abajo. Estructura:

    // [Error:X|N]      <- la unica linea que lee el tester de la catedra
    // [Completo]       <- a partir de aca, la secuencia exacta
    //#lexema,N
    // [Error:Y|N]
    ...
    <fuente>

Las expectativas se escriben con numeros de linea RELATIVOS al fuente (1 = la
primera linea de codigo) y el generador les suma el tamano de la cabecera.
"""
import io
import os

SIN = "resources/sinErrores"
CON = "resources/conErrores"


def E(lexema, linea):
    return ("E", lexema, linea)


def T(lexema, linea):
    return ("T", lexema, linea)


def sin_errores(nombre, fuente, tokens, salto_final=True):
    total = len(fuente) + len(tokens) + 3
    if not salto_final:
        total -= 1
    cuerpo = list(fuente) + [""]
    cuerpo += ["//#%s,%d" % (lex, lin) for lex, lin in tokens]
    cuerpo += ["//#$,%d" % total]
    texto = "\n".join(cuerpo) + ("\n" if salto_final else "")
    io.open(os.path.join(SIN, nombre), "w", encoding="utf-8", newline="\n").write(texto)


def con_errores(nombre, elementos, fuente, salto_final=True, eof=True):
    # 1 linea para la catedra + 1 para [Completo] + una por elemento
    # (+1 si se agrega la expectativa del token EOF).
    desplazamiento = 2 + len(elementos) + (1 if eof else 0)
    primer_error = next(e for e in elementos if e[0] == "E")
    cabecera = ["// [Error:%s|%d]" % (primer_error[1], primer_error[2] + desplazamiento),
                "// [Completo]"]
    for tipo, lexema, linea in elementos:
        absoluta = linea + desplazamiento
        if tipo == "E":
            cabecera.append("// [Error:%s|%d]" % (lexema, absoluta))
        else:
            cabecera.append("//#%s,%d" % (lexema, absoluta))
    if eof:
        lineas_totales = desplazamiento + len(fuente)
        cabecera.append("//#$,%d" % (lineas_totales + (1 if salto_final else 0)))
    texto = "\n".join(cabecera + list(fuente))
    if salto_final:
        texto += "\n"
    io.open(os.path.join(CON, nombre), "w", encoding="utf-8", newline="\n").write(texto)
