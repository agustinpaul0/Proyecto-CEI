#!/usr/bin/env python3
"""Genera `explicacion-reglas.tex`: la gramática final explicada no terminal por
no terminal, con el paso de la transformación del que sale cada uno.

NO es material de entrega: es un documento de estudio. Las producciones salen de
la gramática, así que no pueden quedar viejas; lo escrito a mano son el origen y
la explicación.

Uso, desde `compilador/`:

    python3 herramientas/explicacion_de_reglas.py gramatica/minijava-ll1-final.g > salida.tex
"""
import re
import sys

sys.path.insert(0, "herramientas")
from primeros import parsear  # noqa: E402
from gramatica_a_latex import render  # noqa: E402

# El no terminal con que abre cada sección del `.g`; los `for` van con las sentencias.
SECCIONES = [
    ("Clases e interfaces", "Inicial"),
    ("Miembros", "Miembro"),
    ("Tipos", "Tipo"),
    ("Sentencias", "Bloque"),
    ("Expresiones", "Expresion"),
    ("Referencias y primarios", "Referencia"),
    ("Creación", "Creacion"),
    ("Argumentos actuales", "ArgsActuales"),
]

# De dónde sale cada no terminal: `base` es la gramática de la cátedra; R1-R3 y
# F1-F8, los pasos de la transformación a LL(1); L1-L9, los logros. Un no
# terminal de la base que algún paso modificó lleva también ese paso.
ORIGEN = {
    "Inicial": "base", "ListaClases": "base", "Clase": "base", "Interfaz": "base",
    "GenericidadOpcional": "base", "HerenciaOpcional": "base", "ExtensionOpcional": "base",
    "ListaMiembros": "base", "ListaMetodosInterfaz": "base",
    "Miembro": "base, F6, L6", "MiembroTrasVisibilidad": "L6", "Visibilidad": "L6",
    "VisibilidadOpcional": "L6", "MiembroSinModificadores": "L6", "MiembroDesdeIdClase": "L6",
    "AtributoOMetodo": "F6, L5", "InicializacionDeAtributo": "L5", "Metodo": "base, F6",
    "CuerpoDeMetodo": "F6", "MetodoInterfaz": "base, L6", "PublicOpcional": "L6",
    "TipoMetodo": "base",
    "Tipo": "base", "TipoBase": "base", "DimensionesOpcionales": "base", "TipoReferencia": "base",
    "TipoPrimitivo": "base, L1", "TipoGenericoOpcional": "base",
    "InstanciadoOParametrico": "base, L9", "ArgsFormales": "base",
    "ListaArgsFormalesOpcional": "base", "ListaArgsFormales": "base, R1",
    "RestoListaArgsFormales": "R1", "ArgFormal": "base",
    "Bloque": "base", "ListaSentencias": "base", "Sentencia": "base, F2, L4, L7",
    "SentenciaDesdeIdClase": "L4", "VarLocal": "base, L3", "ListaDeclaradores": "L4",
    "RestoListaDeclaradores": "L4", "InicializacionDeDeclarador": "L4", "Return": "base",
    "ExpresionOpcional": "base", "If": "base, F3", "ElseOpcional": "F3", "While": "base",
    "For": "L7", "EncabezadoFor": "L7", "EncabezadoForTrasVar": "L7", "ClausulasFinalesFor": "L7",
    "Expresion": "base, F1, L3", "RestoExpresion": "F1, L3", "OperadorAsignacion": "base",
    "ExpresionCondicional": "L3", "RestoCondicional": "L3", "ExpresionCompuesta": "base, R2",
    "RestoExpresionCompuesta": "R2", "OperadorBinario": "base", "ExpresionBasica": "base",
    "OperadorUnario": "base", "Operando": "base, L2", "PosfijoOpcional": "L2",
    "Primitivo": "base, L1", "ExpresionSinIdClase": "L4", "ColaTrasOperando": "L4",
    "ColaDeExpresionDesdePrimario": "L4",
    "Referencia": "base, R3", "RestoReferencia": "R3", "Encadenado": "R3",
    "EncadenadoAtributoOMetodo": "R3", "AccesoArreglo": "base", "Primario": "base, F7, F8, L4",
    "LlamadaMetodoEstatico": "base", "PrimarioSinIdClase": "L4", "ExpresionParentizada": "base",
    "AccesoVarOLlamadaMetodo": "F7", "AccesoOLlamada": "F7",
    "Creacion": "F8", "ArregloOInstancia": "F8, L8, L9", "CreacionDesdeIdClase": "L9",
    "GenericoDeCreacion": "L9", "ArregloOInstanciaDeClase": "F8, L8", "CreacionDeArreglo": "L8",
    "RestoCreacionDeArreglo": "L8", "DimensionesConTamanio": "base, F4",
    "DimensionesConTamanioOpcional": "F4", "InicializadorDeArreglo": "L8",
    "ListaElementosOpcional": "L8", "ListaElementos": "L8", "RestoListaElementos": "L8",
    "ElementosTrasComa": "L8", "ElementoInicializador": "L8",
    "ArgsActuales": "base", "ListaExpsOpcional": "base", "ListaExps": "base, F5",
    "RestoListaExps": "F5",
}

# Qué genera cada no terminal, y lo que no es evidente de sus producciones.
EXPLICACION = {
    "Inicial": "Un programa: clases e interfaces y el fin de archivo. Sin el `eof`, lo que sobre "
               "después de la última llave se ignoraría.",
    "ListaClases": "Cero o más clases o interfaces, en cualquier orden: el archivo vacío es "
                   "válido. Punto de recuperación del Modo Pánico.",
    "Clase": "Nombre, parámetro de tipo opcional, herencia opcional y cuerpo.",
    "Interfaz": "Como la clase, pero sólo `extends` y su cuerpo tiene firmas.",
    "GenericidadOpcional": "Un único parámetro de tipo, también con Genericidad Avanzada.",
    "HerenciaOpcional": "Hereda de una clase o implementa una interfaz; nunca las dos ni varias.",
    "ExtensionOpcional": "Una interfaz extiende a lo sumo otra.",
    "ListaMiembros": "Cero o más miembros. Punto de recuperación del Modo Pánico.",
    "ListaMetodosInterfaz": "Cero o más firmas. Punto de recuperación del Modo Pánico.",

    "Miembro": "Empieza con visibilidad, con `static` o sin modificadores. La visibilidad a un "
               "solo lado del `static` acepta `public static` y `static public` y rechaza repetirla.",
    "MiembroTrasVisibilidad": "Tras `public` o `private`: un `static`, que es un método, o un "
                              "miembro sin modificadores.",
    "Visibilidad": "`public` o `private`; no hay `protected`.",
    "VisibilidadOpcional": "La visibilidad opcional después de `static`.",
    "MiembroSinModificadores": "Se decide por el primer token: `void`, un primitivo, `idGen` o "
                               "`idClase`.",
    "MiembroDesdeIdClase": "Tras `idClase`: `(` es un constructor; `<`, `[` o `idMetVar`, un "
                           "atributo o método de tipo referencia.",
    "AtributoOMetodo": "Tras `Tipo idMetVar`: `=` o `;` cierran un atributo y `(` abre un método.",
    "InicializacionDeAtributo": "El `= expresión` opcional del atributo. No se comparte con el de "
                                "las variables locales.",
    "Metodo": "Nombre y cuerpo de un método `static` o `void`. Conserva el nombre de la base pero "
              "deriva sólo esa cola: el método con tipo y sin `static` llega por "
              "`<AtributoOMetodo>`.",
    "CuerpoDeMetodo": "Argumentos formales y bloque: lo mismo en método y constructor.",
    "MetodoInterfaz": "Una firma terminada en `;`: los métodos de interfaz son abstractos.",
    "PublicOpcional": "`public` sí y `private` no: en Java un método de interfaz privado exige "
                      "cuerpo.",
    "TipoMetodo": "Un tipo o `void`.",

    "Tipo": "Tipo base y dimensiones, pegadas al tipo: `int x[]` no es válido.",
    "TipoBase": "Primitivo, referencia o parámetro de tipo.",
    "DimensionesOpcionales": "Cero o más `[]`.",
    "TipoReferencia": "Una clase con su argumento de tipo opcional.",
    "TipoPrimitivo": "Incluye `float`.",
    "TipoGenericoOpcional": "El `<...>` en posición de tipo, sin diamante: `List<> x;` no deriva.",
    "InstanciadoOParametrico": "El argumento de tipo. La recursión por `<TipoGenericoOpcional>` "
                               "permite anidar: `List<List<Persona>>`.",
    "ArgsFormales": "Los parámetros, entre paréntesis.",
    "ListaArgsFormalesOpcional": "La lista de parámetros puede estar vacía.",
    "ListaArgsFormales": "Uno o más parámetros.",
    "RestoListaArgsFormales": "Cero o más `, parámetro`.",
    "ArgFormal": "Tipo y nombre.",

    "Bloque": "Sentencias entre llaves.",
    "ListaSentencias": "Cero o más sentencias. Punto de recuperación del Modo Pánico.",
    "Sentencia": "Las tres últimas alternativas separan la declaración local de la "
                 "sentencia-expresión por el primer token; con `idClase` decide "
                 "`<SentenciaDesdeIdClase>`.",
    "SentenciaDesdeIdClase": "Tras `idClase`: `.` es una llamada estática que empieza una "
                             "expresión; `<`, `[` o `idMetVar`, una declaración.",
    "VarLocal": "Declaración con `var`, siempre inicializada.",
    "ListaDeclaradores": "Una o más variables, cada una con inicialización opcional: "
                         "`int x, y, z = 10;`.",
    "RestoListaDeclaradores": "Cero o más `, variable`.",
    "InicializacionDeDeclarador": "El `= expresión` opcional de una variable local clásica.",
    "Return": "`return` con o sin valor.",
    "ExpresionOpcional": "El valor opcional del `return`.",
    "If": "El `if`, con `else` opcional.",
    "ElseOpcional": "Dangling else: el `else` liga al `if` más cercano, como en Java.",
    "While": "El `while`.",
    "For": "El `for` estándar o de iteradores.",
    "EncabezadoFor": "Empieza con `var` o con una expresión: la variable de control sólo se "
                     "declara con `var`.",
    "EncabezadoForTrasVar": "Tras `var x`: `:` es de iteradores y `=`, estándar.",
    "ClausulasFinalesFor": "Condición e incremento, obligatorios: `for(;;)` no es válido.",

    "Expresion": "Una `<ExpresionCompuesta>` seguida de un `=` o de un ternario. El lado "
                 "izquierdo del `=` es una `<ExpresionCompuesta>`: `a ? b : c = d` no deriva.",
    "RestoExpresion": "Un `=` con su lado derecho, un ternario o nada. El lado derecho no admite "
                      "otro `=`: `a = b = c` no deriva.",
    "OperadorAsignacion": "Sólo `=`.",
    "ExpresionCondicional": "La capa del ternario: lado derecho de los cinco `=` y tercer operando "
                            "del ternario.",
    "RestoCondicional": "El `? :` opcional, asociativo a derecha y con los operandos del "
                        "JLS §15.25: `a ? b = 1 : c` es válido.",
    "ExpresionCompuesta": "La capa de los operadores binarios.",
    "RestoExpresionCompuesta": "Cero o más `operador operando`.",
    "OperadorBinario": "Los trece binarios, en un solo nivel como en la base: la gramática no fija "
                       "precedencia.",
    "ExpresionBasica": "Un operando con o sin un operador unario.",
    "OperadorUnario": "`+`, `-` o `!`, uno solo.",
    "Operando": "Un literal, o una referencia con posfijo opcional.",
    "PosfijoOpcional": "`++` o `--`, uno solo y sobre una referencia: `5++` no deriva.",
    "Primitivo": "Los literales, incluido `floatLiteral`.",
    "ExpresionSinIdClase": "Una expresión que no empieza con `idClase`: la usa `<Sentencia>`.",
    "ColaTrasOperando": "Lo que sigue a un operando: los binarios y el `=` o el ternario.",
    "ColaDeExpresionDesdePrimario": "Lo que sigue a un primario: encadenamientos, posfijo y el "
                                    "resto de la expresión.",

    "Referencia": "Un primario con sus encadenamientos y accesos.",
    "RestoReferencia": "Cero o más `.x`, `.m(...)` o `[i]`, en cualquier orden.",
    "Encadenado": "Un `.nombre`: atributo o llamada.",
    "EncadenadoAtributoOMetodo": "Con argumentos es una llamada; sin ellos, un atributo de otro "
                                 "objeto.",
    "AccesoArreglo": "Un `[expresión]`.",
    "Primario": "La llamada estática o cualquier otro primario.",
    "LlamadaMetodoEstatico": "`Clase.metodo(args)`.",
    "PrimarioSinIdClase": "Los primarios que no empiezan con `idClase`.",
    "ExpresionParentizada": "Una `<Expresion>` completa entre paréntesis: `(y = 2)` es válido.",
    "AccesoVarOLlamadaMetodo": "Un `idMetVar`: variable o llamada.",
    "AccesoOLlamada": "Con argumentos es una llamada; sin ellos, una variable local, un "
                      "parámetro o un atributo propio.",

    "Creacion": "Todo lo que empieza con `new`.",
    "ArregloOInstancia": "Un primitivo o un `idGen` sólo crean arreglos; un `idClase`, arreglos o "
                         "instancias.",
    "CreacionDesdeIdClase": "Tras `new C`: `<` abre el genérico; `[` o `(`, arreglo o instancia.",
    "GenericoDeCreacion": "`>` es el diamante y sólo lleva a `<ArgsActuales>`: `new C<>()` sí, "
                          "`new C<>[3]` no.",
    "ArregloOInstanciaDeClase": "Arreglo o instancia.",
    "CreacionDeArreglo": "Un arreglo, con tamaños o con inicializador.",
    "RestoCreacionDeArreglo": "Con una expresión, la forma de la base, con toda dimensión con "
                              "tamaño; con `]`, la de Java con inicializador, sin ninguno.",
    "DimensionesConTamanio": "Una o más `[expresión]`: `new int[2][]` no es válido.",
    "DimensionesConTamanioOpcional": "Más dimensiones: en `new int[3][4]` el `[` es otra "
                                     "dimensión, como en Java.",
    "InicializadorDeArreglo": "Los elementos entre llaves; sólo después de `new`.",
    "ListaElementosOpcional": "Vacío, sólo la coma (`{,}`) o elementos.",
    "ListaElementos": "Uno o más elementos.",
    "RestoListaElementos": "Una coma y lo que sigue, o nada.",
    "ElementosTrasComa": "Tras una coma, más elementos o nada: la coma final es opcional "
                         "(JLS §10.6).",
    "ElementoInicializador": "Una expresión u otro inicializador: se anidan.",

    "ArgsActuales": "Los argumentos, entre paréntesis.",
    "ListaExpsOpcional": "La lista puede estar vacía.",
    "ListaExps": "Uno o más argumentos.",
    "RestoListaExps": "Una coma y otra lista, o nada.",
}


def secciones_del_archivo(texto):
    """Los no terminales en el orden del `.g`."""
    orden = []
    for linea in texto.split("\n"):
        limpia = linea.strip()
        if limpia.startswith("#") or "->" not in limpia:
            continue
        izquierda = limpia.split("->")[0].strip()
        if izquierda not in orden:
            orden.append(izquierda)
    return orden


def latex_de(texto):
    """Convierte el marcado liviano de las explicaciones a LaTeX."""
    texto = texto.replace("\\", r"\textbackslash{}")
    for viejo, nuevo in (("&", r"\&"), ("%", r"\%"), ("_", r"\_"), ("#", r"\#"),
                         ("$", r"\$"), ("{", r"\{"), ("}", r"\}")):
        texto = texto.replace(viejo, nuevo)
    texto = texto.replace(">>", ">{}>").replace("<<", "<{}<").replace("--", "-{}-")
    return re.sub(r"`([^`]+)`", lambda m: r"\texttt{%s}" % m.group(1), texto)


def main():
    texto = open(sys.argv[1], encoding="utf-8").read()
    g = parsear(texto)
    nts = g.no_terminales
    orden = secciones_del_archivo(texto)
    inicio = {nt: titulo for titulo, nt in SECCIONES}

    faltan = [nt for nt in orden if nt not in EXPLICACION or nt not in ORIGEN]
    if faltan:
        sys.exit("sin explicación u origen: " + ", ".join(faltan))

    print(r"\documentclass[10pt,a4paper]{article}")
    print(r"\input{preambulo}")
    print(r"\begin{document}")
    print(r"\newgeometry{margin=1.8cm}")
    print(r"\pagestyle{plain}")
    print(CARATULA)
    for nt in orden:
        if nt in inicio:
            print(r"\section*{%s}" % inicio[nt])
        print(r"\par\smallskip\noindent\textbf{\nt{%s}}\hfill{\footnotesize\textsf{%s}}"
              r"\nobreak\par\nobreak" % (nt, ORIGEN[nt]))
        print(r"\begingroup\small\setlength{\leftskip}{1em}\raggedright")
        print(r"\hyphenpenalty=10000 \exhyphenpenalty=10000 \hbadness=10000")
        for cuerpo in g.por_izquierda[nt]:
            derecha = " ".join(render(s, nts) for s in cuerpo)
            print(r"\par\hangindent=1.6em\hangafter=1\noindent\deriva{} %s" % derecha)
        print(r"\par\nobreak\noindent %s\par\endgroup" % latex_de(EXPLICACION[nt]))
    print(r"\end{document}")


CARATULA = r"""
\noindent{\Large\bfseries La gramática, regla por regla\par}
\medskip\noindent
{\small A la derecha de cada no terminal, de dónde sale: \textsf{base} es la gramática de la
cátedra; \textsf{R1}--\textsf{R3} y \textsf{F1}--\textsf{F8}, los pasos de la transformación a
LL(1); \textsf{L1}--\textsf{L9}, los logros (informe, §3).\par}
"""

if __name__ == "__main__":
    main()
