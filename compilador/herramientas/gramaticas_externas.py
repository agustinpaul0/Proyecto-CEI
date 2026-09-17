#!/usr/bin/env python3
"""Lo que comparten las dos gramáticas externas que consume el fuzzer.

`gramatica_a_antlr.py` y `gramatica_a_javacc.py` generan la parte sintáctica
desde gramatica/minijava-ll1-final.g, que es la fuente de verdad del proyecto,
pero la parte léxica no está en ese archivo: vive en el analizador de la Etapa 1.
Acá se transcribe una vez, siguiendo
documentacion/lexico/especificacion-lexica-de-minijava.md, y las dos la comparten,
junto con la convención para nombrar las reglas.

Se emiten todos los terminales de la gramática: el compilador reconoce el
lenguaje entero, sin configuración.
"""

# terminal de la gramática -> (nombre de la regla léxica, None)
PALABRAS_CLAVE = [
    ("class", "PR_CLASS", None), ("extends", "PR_EXTENDS", None),
    ("interface", "PR_INTERFACE", None), ("implements", "PR_IMPLEMENTS", None),
    ("static", "PR_STATIC", None), ("boolean", "PR_BOOLEAN", None),
    ("char", "PR_CHAR", None), ("int", "PR_INT", None), ("void", "PR_VOID", None),
    ("public", "PR_PUBLIC", None), ("if", "PR_IF", None), ("else", "PR_ELSE", None),
    ("while", "PR_WHILE", None), ("return", "PR_RETURN", None), ("var", "PR_VAR", None),
    ("this", "PR_THIS", None), ("new", "PR_NEW", None), ("null", "PR_NULL", None),
    ("true", "PR_TRUE", None), ("false", "PR_FALSE", None),
    ("float", "PR_FLOAT", "float"),
    ("for", "PR_FOR", "for"), ("private", "PR_PRIVATE", "visibilidad"),
]

# Los de dos caracteres van antes que los de uno: en ANTLR y en JavaCC gana la
# coincidencia más larga, pero el orden deja explícito el criterio de maximal
# munch con el que el analizador de la Etapa 1 los reconoce.
OPERADORES = [
    ("==", "OP_IGUAL", None), ("!=", "OP_DISTINTO", None),
    (">=", "OP_MAYOR_IGUAL", None), ("<=", "OP_MENOR_IGUAL", None),
    ("&&", "OP_AND", None), ("||", "OP_OR", None),
    ("++", "OP_INCREMENTO", None), ("--", "OP_DECREMENTO", None),
    ("->", "OP_FLECHA", "lambda"),
    (">", "OP_MAYOR", None), ("<", "OP_MENOR", None), ("!", "OP_NEGACION", None),
    ("=", "OP_ASIGNACION", None), ("%", "OP_MODULO", None), ("+", "OP_SUMA", None),
    ("-", "OP_RESTA", None), ("*", "OP_MULT", None), ("/", "OP_DIV", None),
    ("?", "SIGNO_PREGUNTA", "ternario"),
]

PUNTUACION = [
    ("(", "PAREN_A", None), (")", "PAREN_C", None), ("{", "LLAVE_A", None),
    ("}", "LLAVE_C", None), ("[", "CORCHETE_A", None), ("]", "CORCHETE_C", None),
    (";", "PUNTO_Y_COMA", None), (",", "COMA", None), (".", "PUNTO", None),
    (":", "DOS_PUNTOS", None),
]

# Terminales de lexema variable, en el orden en que se resuelven.
VARIABLES = [
    ("floatLiteral", "FLOAT_LITERAL", "float"),
    ("intLiteral", "INT_LITERAL", None),
    ("charLiteral", "CHAR_LITERAL", None),
    ("stringLiteral", "LIT_STRING", None),
    ("idClase", "ID_CLASE", None),
    ("idGen", "ID_GEN", None),
    ("idMetVar", "ID_MV", None),
]

TODOS = PALABRAS_CLAVE + OPERADORES + PUNTUACION + VARIABLES + [("eof", "EOF", None)]


# ANTLR genera código Python y JavaCC genera código Java, así que un no terminal
# que se llame como una palabra reservada de cualquiera de los dos rompe la
# generación: <Return> daría `self.return`, que no compila.
RESERVADAS = {
    "if", "while", "for", "return", "class", "interface", "new", "this", "int",
    "char", "float", "void", "public", "private", "static", "true", "false",
    "null", "else", "var", "import", "lambda", "def", "pass", "not", "and", "or",
    "in", "is", "from", "global", "try", "final",
}


def nombre_de_no_terminal(no_terminal):
    nombre = no_terminal[0].lower() + no_terminal[1:]
    return nombre + "_" if nombre in RESERVADAS else nombre


def nombre_de_regla(terminal):
    for lexema, regla, _ in TODOS:
        if lexema == terminal:
            return regla
    raise KeyError(f"terminal sin regla lexica: {terminal!r}")


def habilitados():
    """Todos los terminales de la gramática."""
    return {lexema for lexema, _, _ in TODOS}


def _activos(tabla):
    return [(lexema, regla) for lexema, regla, _ in tabla]


# --- ANTLR -------------------------------------------------------------------

def _antlr_literal(lexema):
    return "'" + lexema.replace("\\", "\\\\").replace("'", "\\'") + "'"


def lexer_antlr():
    """El bloque léxico del .g4. El orden importa: las palabras clave van antes
    que los identificadores para que `class` no sea un idMV, y ID_CLASE antes que
    ID_GEN aunque ANTLR resuelva por longitud."""
    lineas = []
    for tabla, titulo in ((PALABRAS_CLAVE, "palabras clave"),
                          (OPERADORES, "operadores"),
                          (PUNTUACION, "puntuacion")):
        lineas.append(f"// {titulo}")
        for lexema, regla in _activos(tabla):
            lineas.append(f"{regla} : {_antlr_literal(lexema)} ;")
        lineas.append("")

    lineas.append("// literales e identificadores")
    lineas.append("FLOAT_LITERAL")
    lineas.append("    : DIGITO+ '.' DIGITO* EXPONENTE? SUFIJO?")
    lineas.append("    | DIGITO+ EXPONENTE SUFIJO?")
    lineas.append("    | DIGITO+ SUFIJO")
    lineas.append("    ;")
    # Uno a nueve dígitos: diez ya es un error léxico y el fuzzer no debe generarlo.
    lineas.append("INT_LITERAL : DIGITO DIGITO? DIGITO? DIGITO? DIGITO? DIGITO? DIGITO? DIGITO? DIGITO? ;")
    lineas.append("CHAR_LITERAL : '\\'' ( CHAR_COMUN | ESCAPE ) '\\'' ;")
    lineas.append("LIT_STRING : '\"' ( STRING_COMUN | ESCAPE )* '\"' ;")
    lineas.append("ID_CLASE : MAYUS ( MAYUS | MINUS | DIGITO | '_' )+ ;")
    lineas.append("ID_GEN : MAYUS ;")
    lineas.append("ID_MV : MINUS ( MAYUS | MINUS | DIGITO | '_' )* ;")
    lineas.append("")
    lineas.append("fragment MAYUS : [A-Z] ;")
    lineas.append("fragment MINUS : [a-z] ;")
    lineas.append("fragment DIGITO : [0-9] ;")
    # Imprimibles ASCII sin el delimitador ni la barra: el fuzzer no tiene por
    # qué generar caracteres de control ni pares subrogados, que son casos
    # LÉXICOS y ya los cubre el corpus escrito a mano.
    lineas.append("fragment CHAR_COMUN : [\\u0020-\\u0026\\u0028-\\u005B\\u005D-\\u007E] ;")
    lineas.append("fragment STRING_COMUN : [\\u0020-\\u0021\\u0023-\\u005B\\u005D-\\u007E] ;")
    # Sin `\\uXXXX`: el escape Unicode depende del logro LÉXICO `unicodes`, que no
    # es de esta etapa. Con el logro apagado `'\\u0041'` es un char de seis
    # caracteres, o sea un error léxico, y el corpus sintáctico tiene que valer en
    # cualquier configuración. Los escapes Unicode los cubre el corpus léxico.
    lineas.append("fragment ESCAPE : '\\\\' [btnfr\"'\\\\] ;")
    lineas.append("fragment EXPONENTE : [eE] [+-]? DIGITO+ ;")
    lineas.append("fragment SUFIJO : [fFdD] ;")
    lineas.append("")
    lineas.append("// se descartan")
    lineas.append("COMENTARIO_DE_LINEA : '//' ~[\\r\\n]* -> skip ;")
    lineas.append("COMENTARIO_MULTILINEA : '/*' .*? '*/' -> skip ;")
    lineas.append("BLANCOS : [ \\t\\r\\n]+ -> skip ;")
    return "\n".join(lineas) + "\n"


# --- JavaCC ------------------------------------------------------------------

_ESCAPES_JAVACC = {"\\": "\\\\", '"': '\\"'}


def _javacc_literal(lexema):
    return '"' + "".join(_ESCAPES_JAVACC.get(c, c) for c in lexema) + '"'


def lexer_javacc():
    lineas = ["SKIP :", "{", '      " "', '    | "\\t"', '    | "\\r"', '    | "\\n"',
              '    | <"//" (~["\\r","\\n"])*>',
              '    | <"/*" (~["*"])* ("*" (~["*","/"]) (~["*"])*)* ("*")+ "/">', "}", ""]
    lineas.append("TOKEN :")
    lineas.append("{")
    entradas = []
    for tabla in (PALABRAS_CLAVE, OPERADORES, PUNTUACION):
        for lexema, regla in _activos(tabla):
            entradas.append(f"<{regla}: {_javacc_literal(lexema)}>")
    entradas.append('<FLOAT_LITERAL: (<DIGITO>)+ "." (<DIGITO>)* (<EXPONENTE>)? (<SUFIJO>)?'
                    ' | (<DIGITO>)+ <EXPONENTE> (<SUFIJO>)?'
                    ' | (<DIGITO>)+ <SUFIJO>>')
    entradas.append("<INT_LITERAL: (<DIGITO>) (<DIGITO>)? (<DIGITO>)? (<DIGITO>)? (<DIGITO>)?"
                    " (<DIGITO>)? (<DIGITO>)? (<DIGITO>)? (<DIGITO>)?>")
    entradas.append('<CHAR_LITERAL: "\'" ( <CHAR_COMUN> | <ESCAPE> ) "\'">')
    entradas.append('<LIT_STRING: "\\"" ( <STRING_COMUN> | <ESCAPE> )* "\\"">')
    entradas.append("<ID_CLASE: <MAYUS> ( <MAYUS> | <MINUS> | <DIGITO> | \"_\" )+>")
    entradas.append("<ID_GEN: <MAYUS>>")
    entradas.append("<ID_MV: <MINUS> ( <MAYUS> | <MINUS> | <DIGITO> | \"_\" )*>")
    entradas.append('<#MAYUS: ["A"-"Z"]>')
    entradas.append('<#MINUS: ["a"-"z"]>')
    entradas.append('<#DIGITO: ["0"-"9"]>')
    entradas.append('<#CHAR_COMUN: ["\\u0020"-"\\u0026","\\u0028"-"\\u005b","\\u005d"-"\\u007e"]>')
    entradas.append('<#STRING_COMUN: ["\\u0020"-"\\u0021","\\u0023"-"\\u005b","\\u005d"-"\\u007e"]>')
    entradas.append('<#ESCAPE: "\\\\" ["b","t","n","f","r","\\"","\'","\\\\"]>')
    entradas.append('<#EXPONENTE: ["e","E"] (["+","-"])? (<DIGITO>)+>')
    entradas.append('<#SUFIJO: ["f","F","d","D"]>')
    for i, entrada in enumerate(entradas):
        lineas.append(("      " if i == 0 else "    | ") + entrada)
    lineas.append("}")
    return "\n".join(lineas) + "\n"
