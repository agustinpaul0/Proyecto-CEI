#!/usr/bin/env python3
"""Genera las constantes de Primeros que consume el Analizador Sintáctico.

    python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-logros.g

Escribe una clase por sección de la gramática en
src/main/java/minijava/sintactico/primeros/, con una constante por producción y
su producción citada al lado. La división es la misma que la de
sintactico/partes/, así cada archivo se lee contra una sección de la gramática y
contra su analizador. Se genera y NO se edita a mano: es la única forma de
garantizar que lo que el analizador usa para elegir producción sea exactamente
lo que el verificador calcula sobre la gramática.

Las producciones marcadas con un logro (@+nombre / @-nombre) se emiten igual,
porque el analizador consulta el logro por su cuenta antes de tomar la rama.
"""
import sys
from collections import OrderedDict

sys.path.insert(0, "herramientas")
from primeros import (EPSILON, analizar, dividir_en_alternativas, parsear,  # noqa: E402
                      primeros_de_secuencia)

DIRECTORIO = "src/main/java/minijava/sintactico/primeros"

# El banner de sección de la gramática -> la clase que agrupa sus constantes.
# Es la misma división que sintactico/partes/, para que cada archivo de Primeros
# se lea contra una sección de la gramática y contra un analizador de sección.
POR_SECCION = OrderedDict((
    ("", "PrimerosDeClases"),
    ("MIEMBROS", "PrimerosDeMiembros"),
    ("TIPOS", "PrimerosDeTipos"),
    ("SENTENCIAS", "PrimerosDeSentencias"),
    ("Fors! E2", "PrimerosDeFors"),
    ("EXPRESIONES", "PrimerosDeExpresiones"),
    ("REFERENCIAS Y PRIMARIOS", "PrimerosDeReferencias"),
    ("MiniLambdas E2", "PrimerosDeLambdas"),
    ("CREACIÓN", "PrimerosDeCreacion"),
    ("ARGUMENTOS ACTUALES", "PrimerosDeArgumentos"),
))

DESCRIPCION = {
    "PrimerosDeClases": "La estructura del fuente: clases, interfaces y su cabecera.",
    "PrimerosDeMiembros": "Los miembros de una clase y los metodos de una interfaz.",
    "PrimerosDeTipos": "Los tipos, las dimensiones y los argumentos formales.",
    "PrimerosDeSentencias": "Los bloques y las sentencias.",
    "PrimerosDeFors": "El logro Fors! E2.",
    "PrimerosDeExpresiones": "Las capas de precedencia de las expresiones.",
    "PrimerosDeReferencias": "Las referencias, los primarios y el encadenamiento.",
    "PrimerosDeLambdas": "El logro MiniLambdas E2 y la desambiguacion del parentesis.",
    "PrimerosDeCreacion": "Las expresiones de creacion.",
    "PrimerosDeArgumentos": "La lista de argumentos actuales de una llamada.",
}

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


def secciones_de_las_producciones(texto):
    """A qué sección de la gramática pertenece cada producción, en el mismo orden
    en que parsear() las devuelve. La sección sale de los banners `# ===` del .g,
    que es donde el autor ya declaró la división."""
    secciones = []
    actual = ""
    lineas = texto.splitlines()
    for i, linea in enumerate(lineas):
        limpia = linea.strip()
        if limpia.startswith("#"):
            contenido = limpia.lstrip("#").strip()
            anterior = lineas[i - 1].strip() if i else ""
            siguiente = lineas[i + 1].strip() if i + 1 < len(lineas) else ""
            if (contenido and set(anterior.lstrip("# ")) == {"="}
                    and set(siguiente.lstrip("# ")) == {"="}):
                actual = contenido
            continue
        if "#" in linea:
            linea = linea[:linea.index("#")]
        limpia = linea.strip()
        if "->" not in limpia:
            continue
        _, cuerpo = limpia.split("->", 1)
        if "@" in cuerpo:
            cuerpo = cuerpo[:cuerpo.index("@")]
        for alternativa in dividir_en_alternativas(cuerpo.split()):
            if alternativa:
                secciones.append(actual)
    return secciones


def declaracion(nombre, terminales):
    if not terminales:
        return [f"    public static final ConjuntoDePrimeros {nombre} = de();"]
    valores = [f"TipoDeToken.{POR_TERMINAL[t]}" for t in terminales]
    una_linea = f"    public static final ConjuntoDePrimeros {nombre} = de({', '.join(valores)});"
    if len(una_linea) <= 110:
        return [una_linea]
    lineas = [f"    public static final ConjuntoDePrimeros {nombre} = de("]
    renglon = "            "
    for i, valor in enumerate(valores):
        pieza = valor + ("," if i < len(valores) - 1 else ");")
        if len(renglon) + len(pieza) + 1 > 110:
            lineas.append(renglon.rstrip())
            renglon = "            "
        renglon += pieza + " "
    lineas.append(renglon.rstrip())
    return lineas


def main():
    ruta = sys.argv[1] if len(sys.argv) > 1 else "gramatica/minijava-ll1-logros.g"
    texto = open(ruta, encoding="utf-8").read()
    g = parsear(texto)
    vaciables, primeros_nt, _ = analizar(g)

    secciones = secciones_de_las_producciones(texto)
    if len(secciones) != len(g.producciones):
        sys.exit(f"ERROR: {len(secciones)} secciones para {len(g.producciones)} producciones")

    por_clase = OrderedDict((clase, []) for clase in POR_SECCION.values())
    usados = set()
    for (izquierda, cuerpo), seccion in zip(g.producciones, secciones):
        if seccion not in POR_SECCION:
            sys.exit(f"ERROR: seccion desconocida en la gramatica: {seccion!r}")
        conjunto = primeros_de_secuencia(g, primeros_nt, vaciables, cuerpo)
        terminales = sorted(t for t in conjunto if t != EPSILON)
        nombre = nombre_de_constante(izquierda, cuerpo, usados)
        por_clase[POR_SECCION[seccion]].append((nombre, izquierda, cuerpo, terminales))

    total = 0
    for clase, constantes in por_clase.items():
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
            f"// {DESCRIPCION[clase]}",
            "//",
            "// Una constante por produccion, con su produccion citada al lado, para que",
            "// el analizador se lea contra la gramatica del informe renglon por renglon.",
            "// Las producciones que existen por un logro se emiten igual: el analizador",
            "// consulta el logro por su cuenta antes de tomar la rama.",
            f"public final class {clase} {{",
            "",
        ]
        for nombre, izquierda, cuerpo, terminales in constantes:
            lineas.append(f"    // {izquierda} -> {' '.join(cuerpo)}")
            lineas += declaracion(nombre, terminales)
            lineas.append("")
        lineas += [f"    private {clase}() {{", "    }", "}", ""]

        destino = f"{DIRECTORIO}/{clase}.java"
        with open(destino, "w", encoding="utf-8") as archivo:
            archivo.write("\n".join(lineas))
        print(f"{destino}: {len(constantes)} constantes")
        total += len(constantes)
    print(f"{total} constantes en {len(por_clase)} clases")


if __name__ == "__main__":
    sys.exit(main())
