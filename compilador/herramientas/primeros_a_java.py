#!/usr/bin/env python3
"""Genera las constantes de Primeros que consume el Analizador Sintáctico.

    python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-logros.g

Escribe src/main/java/minijava/sintactico/primeros/Primeros.java, una constante
por producción, con su producción citada al lado. Se genera y NO se edita a
mano: es la única forma de garantizar que lo que el analizador usa para elegir
producción sea exactamente lo que el verificador calcula sobre la gramática.

Las producciones marcadas con un logro (@+nombre / @-nombre) se emiten igual,
porque el analizador consulta el logro por su cuenta antes de tomar la rama.
"""
import sys
from collections import OrderedDict

sys.path.insert(0, "herramientas")
from primeros import EPSILON, analizar, parsear, primeros_de_secuencia  # noqa: E402

DESTINO = "src/main/java/minijava/sintactico/primeros/Primeros.java"

# El terminal de la gramática -> la constante de TipoDeToken que lo nombra.
POR_TERMINAL = {
    "class": "PR_CLASS", "extends": "PR_EXTENDS", "interface": "PR_INTERFACE",
    "implements": "PR_IMPLEMENTS", "static": "PR_STATIC", "boolean": "PR_BOOLEAN",
    "char": "PR_CHAR", "int": "PR_INT", "void": "PR_VOID", "public": "PR_PUBLIC",
    "if": "PR_IF", "else": "PR_ELSE", "while": "PR_WHILE", "return": "PR_RETURN",
    "var": "PR_VAR", "this": "PR_THIS", "new": "PR_NEW", "null": "PR_NULL",
    "true": "PR_TRUE", "false": "PR_FALSE", "float": "PR_FLOAT",
    "for": "PR_FOR", "private": "PR_PRIVATE",
    "idClase": "ID_CLASE", "idGen": "ID_GEN", "idMetVar": "ID_MV",
    "intLiteral": "INT_LITERAL", "charLiteral": "CHAR_LITERAL",
    "stringLiteral": "LIT_STRING", "floatLiteral": "FLOAT_LITERAL",
    "(": "PAREN_A", ")": "PAREN_C", "{": "LLAVE_A", "}": "LLAVE_C",
    "[": "CORCHETE_A", "]": "CORCHETE_C", ";": "PUNTO_Y_COMA", ",": "COMA",
    ".": "PUNTO", ":": "DOS_PUNTOS",
    ">": "OP_MAYOR", "<": "OP_MENOR", "!": "OP_NEGACION", "=": "OP_ASIGNACION",
    "==": "OP_IGUAL", ">=": "OP_MAYOR_IGUAL", "<=": "OP_MENOR_IGUAL",
    "!=": "OP_DISTINTO", "&&": "OP_AND", "||": "OP_OR", "%": "OP_MODULO",
    "+": "OP_SUMA", "-": "OP_RESTA", "*": "OP_MULT", "/": "OP_DIV",
    "++": "OP_INCREMENTO", "--": "OP_DECREMENTO", "->": "OP_FLECHA",
    "?": "SIGNO_PREGUNTA",
    "eof": "EOF",
}

# Para nombrar la constante de cada alternativa: el primer símbolo del cuerpo,
# en MAYUSCULAS_CON_GUION.
NOMBRE_DE_SIMBOLO = {
    "(": "PAREN_A", ")": "PAREN_C", "{": "LLAVE_A", "}": "LLAVE_C",
    "[": "CORCHETE_A", "]": "CORCHETE_C", ";": "PUNTO_Y_COMA", ",": "COMA",
    ".": "PUNTO", ":": "DOS_PUNTOS",
    ">": "MAYOR", "<": "MENOR", "!": "NEGACION", "=": "ASIGNACION",
    "==": "IGUAL", ">=": "MAYOR_IGUAL", "<=": "MENOR_IGUAL", "!=": "DISTINTO",
    "&&": "AND", "||": "OR", "%": "MODULO", "+": "SUMA", "-": "RESTA",
    "*": "MULT", "/": "DIV", "++": "INCREMENTO", "--": "DECREMENTO",
    "->": "FLECHA", "?": "SIGNO_PREGUNTA",
}


def a_mayusculas(nombre):
    """CamelCase -> CAMEL_CASE."""
    salida = []
    for i, caracter in enumerate(nombre):
        if caracter.isupper() and i and not nombre[i - 1].isupper():
            salida.append("_")
        salida.append(caracter.upper())
    return "".join(salida)


def nombre_de_simbolo(simbolo):
    if simbolo in NOMBRE_DE_SIMBOLO:
        return NOMBRE_DE_SIMBOLO[simbolo]
    return a_mayusculas(simbolo)


def nombre_de_constante(izquierda, cuerpo, usados):
    base = a_mayusculas(izquierda)
    if cuerpo != [EPSILON]:
        base += "_" + nombre_de_simbolo(cuerpo[0])
    nombre, sufijo = base, 2
    while nombre in usados:
        nombre, sufijo = f"{base}_{sufijo}", sufijo + 1
    usados.add(nombre)
    return nombre


def main():
    ruta = sys.argv[1] if len(sys.argv) > 1 else "gramatica/minijava-ll1-logros.g"
    with open(ruta, encoding="utf-8") as archivo:
        g = parsear(archivo.read())
    vaciables, primeros_nt, _ = analizar(g)

    constantes = OrderedDict()
    usados = set()
    for izquierda, cuerpo in g.producciones:
        conjunto = primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo)
        terminales = sorted(t for t in conjunto if t != EPSILON)
        nombre = nombre_de_constante(izquierda, cuerpo, usados)
        constantes[nombre] = (izquierda, cuerpo, terminales)

    lineas = [
        "package minijava.sintactico.primeros;",
        "",
        "import minijava.lexico.tokens.TipoDeToken;",
        "",
        "import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;",
        "",
        "// GENERADO por herramientas/primeros_a_java.py desde",
        f"// {ruta}. No editar a mano: se regenera.",
        "//",
        "// Una constante por produccion, con su produccion citada al lado, para que",
        "// el analizador se lea contra la gramatica del informe renglon por renglon.",
        "// Las producciones que existen por un logro se emiten igual: el analizador",
        "// consulta el logro por su cuenta antes de tomar la rama.",
        "public final class Primeros {",
        "",
    ]
    for nombre, (izquierda, cuerpo, terminales) in constantes.items():
        lineas.append(f"    // {izquierda} -> {' '.join(cuerpo)}")
        if not terminales:
            lineas.append(f"    public static final ConjuntoDePrimeros {nombre} = de();")
        else:
            valores = [f"TipoDeToken.{POR_TERMINAL[t]}" for t in terminales]
            cuerpo_java = ", ".join(valores)
            declaracion = f"    public static final ConjuntoDePrimeros {nombre} = de({cuerpo_java});"
            if len(declaracion) <= 110:
                lineas.append(declaracion)
            else:
                lineas.append(f"    public static final ConjuntoDePrimeros {nombre} = de(")
                renglon = "            "
                for i, valor in enumerate(valores):
                    pieza = valor + ("," if i < len(valores) - 1 else ");")
                    if len(renglon) + len(pieza) + 1 > 110:
                        lineas.append(renglon.rstrip())
                        renglon = "            "
                    renglon += pieza + " "
                lineas.append(renglon.rstrip())
        lineas.append("")

    lineas += ["    private Primeros() {", "    }", "}", ""]

    with open(DESTINO, "w", encoding="utf-8") as archivo:
        archivo.write("\n".join(lineas))
    print(f"{DESTINO}: {len(constantes)} constantes")


if __name__ == "__main__":
    sys.exit(main())
