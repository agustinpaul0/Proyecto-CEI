#!/usr/bin/env python3
"""Lote 7: bordes de los comentarios.

Hallazgo que motiva el lote: un comentario de LINEA no puede estar mal formado
-- `//` se cierra con '\n' o con el EOF y las dos terminaciones son validas, asi
que EstadoComentarioDeLinea no tiene ninguna rama de error. El unico error de la
familia es COMENTARIO_SIN_CERRAR, exclusivo de `/* */`. Lo que faltaba cubrir,
entonces, no son "comentarios mal formados" sino los bordes: EOF pegado,
controles crudos adentro, y las construcciones que un comentario NO abre.

Los casos que terminan exactamente en el constructo (`//`, `/*`, `/` como ultimo
caracter del archivo) no entran en este formato -- las lineas de oraculo son
comentarios y quedarian despues -- y viven como unit tests en las suites AnalizadorLexico*Test.

Expectativas predichas a mano antes de correr, como manda herramientas/README.md.
"""
from generar_casos import E, T, sin_errores, con_errores

# --- sinErrores: lo que un comentario de linea se traga sin inmutarse ---------

sin_errores("comentarioDeLineaConLiteralesSinCerrar.java", [
    '// "un litString sin cerrar no abre nada',
    "// 'a tampoco un charLiteral",
    "// /* ni un comentario multilinea",
    "a",
], [("a", 4)])

sin_errores("comentarioDeLineaTerminadoEnBarraInvertida.java", [
    "// Java no tiene continuacion de linea: esta barra no sigue \\",
    "a",
], [("a", 2)])

sin_errores("comentarioDeLineaVacioEntreTokens.java", [
    "a//",
    "b",
    "c//",
], [("a", 1), ("b", 2), ("c", 3)])

# --- conErrores --------------------------------------------------------------

# El // se traga el resto de la linea: un solo error, no dos.
con_errores("comentarioDeLineaDespuesDeUnError.java",
            [E("@", 1), T("v1", 2)],
            ["@ // el resto de la linea se descarta y aca no hay segundo error",
             "v1"])

# La apertura queda sola en su linea: el error reporta la linea de APERTURA
# aunque el EOF caiga una linea mas abajo.
con_errores("comentarioSinCerrarConSaltoInmediato.java",
            [T("v1", 1), E("/*", 2)],
            ["v1",
             "/*"])

# Podados tras medirlos: cuatro casos mas (centinela dentro de // y dentro de un
# /* sin cerrar, "// /*" que tapa una apertura, y /* abierto en la ultima columna)
# no movian ni una linea ni una rama de cobertura, y lo unico que aportaban ya lo
# fijan las suites AnalizadorLexico*Test y PosicionDelCaretTest. No se agregan al
# corpus: documentacion/compartido/estrategia-de-testing.md advierte contra
# ensuciarlo con duplicados.

# El caso de fin de linea mezclado se escribe a mano en el propio corpus: necesita
# CRLF y CR sueltos, y generar_casos.py fuerza newline="\n".

print("lote7: 5 casos generados")
