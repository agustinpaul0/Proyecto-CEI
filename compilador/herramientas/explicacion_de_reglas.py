#!/usr/bin/env python3
"""Genera `explicacion-reglas.tex`: la gramática final explicada no terminal por
no terminal — qué genera cada regla y por qué está escrita así.

NO es material de entrega: es un documento de estudio, para tener a mano qué hace
cada producción sin releer el informe. Las producciones salen de la gramática, así
que no pueden quedar viejas; lo único escrito a mano son las explicaciones.

Uso, desde `compilador/`:

    python3 herramientas/explicacion_de_reglas.py gramatica/minijava-ll1-final.g > salida.tex
"""
import re
import sys

sys.path.insert(0, "herramientas")
from primeros import EPSILON, parsear  # noqa: E402
from gramatica_a_latex import escapar, render  # noqa: E402

# Las ocho secciones del `.g`, con el no terminal que abre cada una. El orden es
# el del archivo, que es el orden en que conviene leerlas.
SECCIONES = [
    ("Clases e interfaces", "Inicial",
     "El nivel de arriba: un programa es una lista de clases e interfaces, y nada más. "
     "Acá se fija qué puede heredar cada una y qué puede contener."),
    ("Miembros", "Miembro",
     "Lo que va adentro de una clase o de una interfaz. Es donde vive la factorización más "
     "profunda de la gramática, porque atributo, método y constructor comparten prefijo."),
    ("Tipos", "Tipo",
     "Los tipos y las listas de argumentos formales. Casi todo se hereda tal cual de la "
     "gramática de la cátedra."),
    ("Sentencias", "Bloque",
     "El cuerpo de un método. La factorización del `idClase` es la única parte complicada: "
     "una declaración local y una sentencia-expresión pueden empezar igual."),
    ("Expresiones", "Expresion",
     "La jerarquía de precedencias, de arriba hacia abajo: asignación, lambda, ternario, "
     "operadores binarios, unarios, operando. Cada capa admite menos que la de arriba."),
    ("Referencias y primarios", "Referencia",
     "El encadenamiento (`a.b.c()[0]`) y los operandos atómicos. Acá viven también las "
     "producciones que desambiguan el paréntesis de una lambda."),
    ("Creación", "Creacion",
     "Todo lo que sigue a un `new`: instancia, arreglo, genérico y el inicializador."),
    ("Argumentos actuales", "ArgsActuales",
     "La lista de argumentos de una llamada. Sección propia porque la usan por igual el "
     "`new`, el encadenado, la llamada estática y el encabezado del `for`."),
]

# Qué genera cada no terminal, y por qué está escrito así. Una entrada por no
# terminal; las que empiezan con «INSERTADO» son las que no existen en la
# gramática de la cátedra y aparecieron al corregirla.
EXPLICACION = {
    # --- Clases e interfaces ---
    "Inicial": "El símbolo inicial. Un programa es una lista de clases e interfaces seguida "
               "del fin de archivo. Exigir el `eof` es lo que hace que sobre basura después "
               "de la última llave sea un error y no se ignore en silencio.",
    "ListaClases": "Cero o más clases o interfaces, en cualquier orden y mezcladas. Que derive "
                   "a ε es lo que hace válido el archivo vacío.",
    "Clase": "Una clase: nombre, parámetro de tipo opcional, herencia opcional y el cuerpo "
             "entre llaves.",
    "Interfaz": "Una interfaz. Se diferencia de la clase en dos cosas: sólo puede `extends` "
                "(nunca `implements`) y su cuerpo admite firmas, no métodos con cuerpo.",
    "GenericidadOpcional": "El parámetro de tipo de la declaración: `class Pila<T>`. Uno solo, "
                           "nunca dos, incluso con el logro de genericidad avanzada — el "
                           "enunciado lo dice explícitamente.",
    "HerenciaOpcional": "Una clase hereda de UNA clase o implementa UNA interfaz, nunca las dos "
                        "ni varias. Java admite `extends A implements B, C`; MiniJava no, y no "
                        "se amplía. Que el padre sea realmente una clase lo verifica el semántico.",
    "ExtensionOpcional": "Lo mismo para la interfaz, que sólo puede `extends`.",
    "ListaMiembros": "Cero o más miembros de una clase. Uno de los tres puntos donde el "
                     "analizador recupera en Modo Pánico.",
    "ListaMetodosInterfaz": "Cero o más firmas de método en una interfaz.",

    # --- Miembros ---
    "Miembro": "La cabeza de la factorización de los miembros. Tres alternativas con Primeros "
               "disjuntos: empieza con visibilidad, empieza con `static`, o no lleva modificador. "
               "Que la visibilidad aparezca a UN SOLO lado del `static` es lo que acepta "
               "`public static` y `static public` y a la vez rechaza `public private`, sin "
               "ningún chequeo fuera de la gramática.",
    "MiembroTrasVisibilidad": "INSERTADO. Lo que puede seguir a un `public` o un `private`: el "
                              "`static` (y entonces es un método) o un miembro sin modificadores.",
    "Visibilidad": "`public` o `private`. `protected` no existe en MiniJava y no se agrega.",
    "VisibilidadOpcional": "La visibilidad después del `static`, que puede no estar.",
    "MiembroSinModificadores": "Un miembro sin `public`, `private` ni `static`. Las cuatro "
                               "alternativas se distinguen por su primer token: `void`, un tipo "
                               "primitivo, un `idGen` o un `idClase`.",
    "MiembroDesdeIdClase": "INSERTADO, y el punto de decisión más delicado de esta sección. Un "
                           "miembro que empieza con `idClase` puede ser un constructor "
                           "(`C(){}`) o un atributo o método de tipo referencia (`C x;`). El "
                           "desempate es el token siguiente: `(` es constructor, cualquier otro "
                           "abre un tipo.",
    "AtributoOMetodo": "INSERTADO. Después de `Tipo idMetVar`, un `;` o un `=` cierran un "
                       "atributo y un `(` abre un método. Es la factorización que fusiona "
                       "`<Atributo>` y `<Metodo>` de la gramática original.",
    "InicializacionDeAtributo": "El `= expresión` opcional de un atributo. No se comparte con el "
                                "de la variable local a propósito: son construcciones distintas y "
                                "el semántico las trata distinto.",
    "Metodo": "El nombre del método y su cuerpo. Sobrevive como no terminal propio para que la "
              "rama `static` y la rama `void` compartan un único lugar donde el semántico va a "
              "construir el nodo de método.",
    "CuerpoDeMetodo": "Argumentos formales y bloque. Se comparte a propósito entre método, "
                      "método `static` y constructor: es la misma construcción en los tres.",
    "MetodoInterfaz": "Una firma de método en una interfaz, siempre terminada en `;`. Los "
                      "métodos de interfaz de MiniJava son SIEMPRE abstractos.",
    "PublicOpcional": "Un método de interfaz admite `public` y NO `private`, y la asimetría no "
                      "es una preferencia. En Java un método de interfaz `private` exige cuerpo "
                      "(«missing method body, or declare abstract»), y en MiniJava no hay forma "
                      "de escribirlo porque la firma termina en `;`. Un `private` acá sería "
                      "siempre la forma que Java rechaza.",
    "TipoMetodo": "Un tipo o `void`. `void` sólo vale como tipo de retorno, nunca como tipo de "
                  "una variable, y por eso es un no terminal aparte de `<Tipo>`.",

    # --- Tipos ---
    "Tipo": "Un tipo base con sus dimensiones. Las dimensiones van pegadas al TIPO y nunca al "
            "nombre: `int[] x` es válido y `int x[]` no. Java admite las dos formas; MiniJava "
            "define ésta, y no se amplía.",
    "TipoBase": "Primitivo, referencia o parámetro de tipo.",
    "DimensionesOpcionales": "Cero o más pares de corchetes vacíos. Es la recursión a derecha "
                             "que permite `int[][][] x`.",
    "TipoReferencia": "Un `idClase` con su argumento de tipo opcional.",
    "TipoPrimitivo": "`boolean`, `char`, `int` y `float`. El `float` no está en las Reglas de "
                     "Sintaxis de la cátedra: se agrega para que el logro de floats de la "
                     "Etapa 1 siga sirviendo en ésta.",
    "TipoGenericoOpcional": "El `<T>` de un tipo. En posición de TIPO no se admite el diamante: "
                            "`List<> x;` es inválido, y eso lo impone la gramática porque esta "
                            "producción exige un `<ArgumentoDeTipo>` entre los ángulos.",
    "ArgumentoDeTipo": "Lo que va adentro de los ángulos. La recursión por "
                       "`<TipoGenericoOpcional>` es lo que habilita el anidamiento "
                       "`List<List<Persona>>` del logro de genericidad avanzada.",
    "ArgsFormales": "La lista de parámetros de un método o constructor, entre paréntesis.",
    "ListaArgsFormalesOpcional": "INSERTADO. La lista puede estar vacía: `m()`.",
    "ListaArgsFormales": "Al menos un parámetro. La recursión a izquierda de la regla original "
                         "se eliminó pasándola a derecha.",
    "RestoListaArgsFormales": "INSERTADO por la eliminación de recursión a izquierda. Cero o más "
                              "`, parámetro`.",
    "ArgFormal": "Un parámetro: tipo y nombre. Siempre lleva tipo — a diferencia del parámetro "
                 "de una lambda, que nunca lo lleva.",

    # --- Sentencias ---
    "Bloque": "Sentencias entre llaves.",
    "ListaSentencias": "Cero o más sentencias. El tercer punto de recuperación en Modo Pánico.",
    "Sentencia": "Las once formas de sentencia, todas con Primeros disjuntos. Las cuatro "
                 "últimas son la factorización del `idClase`: una declaración local (`C x;`) y "
                 "una sentencia-expresión (`C.m();`) empiezan igual.",
    "SentenciaDesdeIdClase": "INSERTADO, y el desempate de esa factorización: un `.` significa "
                             "que era una expresión, cualquier otro token que era una "
                             "declaración.",
    "VarLocal": "La declaración con `var`, que SIEMPRE exige inicialización — sin el valor no "
                "hay tipo que inferir.",
    "ListaDeclaradores": "Varias variables en una sentencia, cada una con su inicialización "
                         "opcional: `int x, y, z = 10;`.",
    "RestoListaDeclaradores": "INSERTADO. Cero o más `, variable`.",
    "InicializacionDeDeclarador": "El `= expresión` opcional de una variable local clásica.",
    "Return": "`return` con o sin valor. Que el valor corresponda al tipo del método es "
              "semántico.",
    "ExpresionOpcional": "INSERTADO. Una expresión que puede no estar: el valor del `return` y "
                         "las dos cláusulas finales del `for`.",
    "If": "El `if` con su rama `else` opcional.",
    "ElseOpcional": "INSERTADO por la factorización del `if`. Es el *dangling else*: la "
                    "ambigüedad clásica, que no se resuelve reescribiendo la gramática sino con "
                    "una regla de implementación — se toma la rama `else` apenas se ve el token, "
                    "que la liga al `if` más cercano, como en Java.",
    "While": "El `while` clásico.",
    "For": "El `for`, con todo el trabajo en el encabezado.",
    "EncabezadoFor": "Las seis formas del encabezado, factorizadas. Cubre el `for` estándar "
                     "(`for(int i=0; i<n; i++)`) y el de iteradores (`for(var e : lista)`), que "
                     "comparten prefijo hasta el `:` o el `=`.",
    "EncabezadoForDesdeIdClase": "INSERTADO. El mismo desempate que en `<Sentencia>`: un "
                                 "encabezado que empieza con `idClase` puede ser una declaración "
                                 "o una expresión.",
    "EncabezadoForTrasVar": "INSERTADO. Después de `var x`, un `:` lo vuelve un for de "
                            "iteradores y un `=` un for estándar.",
    "EncabezadoForTrasDeclarador": "INSERTADO. Lo mismo para un declarador con tipo explícito.",
    "InicializacionDeDeclaradorFor": "INSERTADO, y deliberadamente NO compartido con el de la "
                                     "variable local: el `for` declara su propia variable, así "
                                     "que `for (int i = 0; ; )` funciona por sí mismo.",
    "ClausulasFinalesFor": "INSERTADO. Las dos cláusulas después del inicializador: condición e "
                           "incremento, las dos opcionales. `for(;;)` es válido.",

    # --- Expresiones ---
    "Expresion": "El nivel MÁS ALTO de la jerarquía: admite una lambda y admite un `=`. Es el "
                 "equivalente de `Expression` en el JLS.",
    "RestoExpresion": "INSERTADO. El `= expresión` opcional. Que el lado derecho sea una "
                      "`<ExpresionCondicionalOLambda>` y no una `<Expresion>` es lo que impide "
                      "`a = b = c`, tal cual la gramática original.",
    "OperadorAsignacion": "Sólo `=`. MiniJava no tiene `+=` ni los demás compuestos.",
    "ExpresionCondicionalOLambda": "El nivel `LambdaExpression | ConditionalExpression` del JLS: "
                                   "admite lambda y NO admite `=`. Es el operando del `=` y el "
                                   "tercero del ternario.",
    "ExpresionDesdePrimarioOLambda": "INSERTADO. Las seis cabezas de una expresión que puede ser "
                                     "una lambda. Las dos que dan trabajo son `idMetVar` y `(`, "
                                     "que abren tanto una lambda como una expresión normal.",
    "RestoCondicional": "INSERTADO. El `? a : b` opcional. Asociativo a DERECHA, como en Java, "
                        "porque la rama del `:` vuelve a este mismo nivel. El operando del medio "
                        "es una `<Expresion>` completa y el de la derecha no, que es exactamente "
                        "lo que acepta y rechaza javac.",
    "RestoExpresionCompuesta": "INSERTADO por la eliminación de recursión a izquierda. Cero o "
                               "más `operador operando`.",
    "OperadorBinario": "Los trece binarios en UN SOLO nivel: MiniJava no define precedencia "
                       "entre ellos, así que `a + b * c` se asocia a izquierda y da `(a+b)*c`. "
                       "No es un descuido de este trabajo: está así en la gramática de la "
                       "cátedra y se preserva.",
    "ExpresionBasica": "Un operando, con o sin operador unario delante.",
    "OperadorUnario": "`+`, `-` y `!`. Los dos primeros son también binarios, y por eso "
                      "`a + + b` es válido — es `a + (+b)`, igual que en Java.",
    "Operando": "Un literal o una referencia con posfijo opcional.",
    "PosfijoOpcional": "El `++` o `--` posfijo. Va sobre `<Referencia>` y no sobre `<Operando>` "
                       "a propósito: `5++` no tiene sentido y la gramática lo rechaza sola.",
    "Primitivo": "Los literales. `floatLiteral` es el agregado del logro de floats.",
    "ExpresionSinIdClase": "INSERTADO. Una expresión que NO puede empezar con `idClase`, porque "
                           "ese token ya lo consumió `<Sentencia>` al factorizar. Sus dos últimas "
                           "producciones son las cabezas ambiguas `idMetVar` y `(` en su versión "
                           "que admite lambda: de ahí que `p -> x;` sea una sentencia válida. El "
                           "`<RestoExpresion>` de atrás es lo único que las distingue de las de "
                           "`<Expresion>`, porque acá la expresión sí lleva el `=`.",
    "ColaTrasOperando": "INSERTADO. La cola completa después de un operando, con asignación.",
    "ColaTrasOperandoSinAsignacion": "INSERTADO. La misma cola pero sin el `=`, para los "
                                     "contextos donde una asignación no es válida.",
    "ColaDeExpresionDesdePrimario": "INSERTADO. Lo que puede seguir a un primario: "
                                    "encadenamiento, posfijo y el resto de la expresión.",
    "ColaDesdePrimarioSinAsignacion": "INSERTADO. Ídem sin asignación. Los pares "
                                      "con/sin asignación existen porque el lado derecho de un "
                                      "`=` y el cuerpo de una lambda no admiten otro `=`.",

    # --- Referencias y primarios ---
    "Referencia": "Un primario con su cadena de encadenamientos y accesos a arreglo.",
    "RestoReferencia": "INSERTADO por la eliminación de recursión a izquierda. Cero o más `.x` o "
                       "`[i]`, en cualquier orden y cantidad: `a.b[0].c()[1]`.",
    "Encadenado": "Un `.nombre`, que puede ser un atributo o una llamada.",
    "EncadenadoAtributoOMetodo": "INSERTADO. El `(` decide: con argumentos es una llamada, sin "
                                 "ellos un acceso a atributo.",
    "AccesoArreglo": "Un `[expresión]`. Que el receptor sea realmente un arreglo es semántico.",
    "Primario": "Un operando atómico. Una lambda SIN paréntesis nunca es un primario —ésa es la "
                "corrección de precedencia del logro—, pero entre paréntesis sí, porque "
                "`<Parentizada>` contiene una `<Expresion>` completa.",
    "LlamadaMetodoEstatico": "`Clase.metodo(args)`.",
    "PrimarioSinIdClase": "INSERTADO. Los primarios que no empiezan con `idClase`.",
    "PrimarioSinIdClaseNiLambda": "INSERTADO. Los tres primarios que, además, no pueden empezar "
                                  "una lambda. Existen aparte porque `<ExpresionSinIdClase>` "
                                  "necesita justo eso: las otras dos cabezas las toma en su "
                                  "variante que sí la admite.",
    "Parentizada": "Un paréntesis común, con una `<Expresion>` completa adentro —lambda "
                   "incluida—, tal cual la regla original. Como un `<Primario>` arrastra cadena, "
                   "de acá sale que `(x -> y).m()` sea válido. Java lo rechaza; se respetó el "
                   "molde de la gramática base.",
    "AccesoVarOLlamada": "Un `idMetVar` que puede ser una variable o una llamada.",
    "AccesoLlamada": "INSERTADO. El `(args)` opcional que distingue los dos usos.",
    "AccesoLlamadaOLambda": "INSERTADO. La versión que además admite `->`: acá se decide si el "
                            "`idMetVar` que se acaba de leer era el único parámetro de una "
                            "lambda o el arranque de una expresión normal. Un token de "
                            "anticipación.",
    "ParentizadaOLambda": "INSERTADO. El `(` que puede abrir una expresión parentizada o la "
                          "lista de parámetros de una lambda.",
    "ContenidoDeParentesis": "INSERTADO. Dos alternativas: un `)` inmediato sólo puede ser una "
                             "lambda de cero parámetros; cualquier otra cosa se lee como "
                             "expresión y se decide después.",
    "RestoDeParentesis": "INSERTADO. Una `,` sólo puede ser una lambda, porque MiniJava no tiene "
                         "operador coma. Es el segundo de los cuatro puntos donde parecía hacer "
                         "falta anticipación ilimitada y alcanza con un token.",
    "FinDeParentizadaOLambda": "INSERTADO. Cerrado el paréntesis, un `->` lo vuelve una lambda y "
                               "cualquier otra cosa lo deja como expresión parentizada. Es el "
                               "cuarto punto de decisión, y el que hace que no haga falta "
                               "escanear hasta el `)`.",
    "ListaParametrosLambda": "Los parámetros de una lambda, sin tipo. Del segundo en adelante la "
                             "gramática exige `idMetVar`; que el PRIMERO también lo sea lo "
                             "verifica el analizador, porque hasta ver el `->` es una expresión.",
    "RestoListaParametrosLambda": "INSERTADO. Cero o más `, parámetro`.",
    "CuerpoLambda": "Una única expresión, sin llaves. Como no hay alternativa con `{`, la "
                    "gramática rechaza `x -> { return x; }` sin ningún chequeo adicional. Es "
                    "voraz: `x -> a + b` es `x -> (a + b)`.",

    # --- Creación ---
    "Creacion": "Todo lo que empieza con `new`.",
    "ArregloOInstancia": "INSERTADO por la factorización del `new`. El tipo decide qué puede "
                         "venir después: un primitivo o un `idGen` sólo pueden crear un arreglo; "
                         "un `idClase` puede crear las dos cosas.",
    "CreacionDesdeIdClase": "INSERTADO. Después de `new C`, un `<` abre un genérico, un `[` un "
                            "arreglo y un `(` una instancia.",
    "GenericoDeCreacion": "INSERTADO. El diamante `<>` sólo se admite en la rama que va a "
                          "`<ArgsActuales>`: `new C<>()` es válido y `new C<>[3]` no. La "
                          "restricción del logro queda impuesta por la gramática, no delegada al "
                          "semántico.",
    "ArregloOInstanciaDeClase": "INSERTADO. Cerrado el genérico, arreglo o instancia.",
    "CreacionDeArreglo": "La creación de un arreglo: sus dimensiones y un inicializador "
                         "opcional. Acá está la divergencia deliberada con Java, y es de una "
                         "sola producción.",
    "DimensionesConTamanio": "Al menos una dimensión, y TODAS con su tamaño — es la forma de "
                             "MiniJava base, que no tiene corchete vacío en una creación. "
                             "`new int[2][2]{{1,2},{3,4}}` es válido y `new int[]{1,2,3}` no, al "
                             "revés que en Java. Se eligió no ampliar una construcción que la "
                             "especificación ya definía.",
    "DimensionesConTamanioOpcional": "INSERTADO por la factorización. Las dimensiones que "
                                     "siguen. Es la ambigüedad heredada de `new int[3][4]`: el "
                                     "`[` se consume como otra dimensión de la creación y no "
                                     "como un acceso al arreglo recién creado, que es lo que "
                                     "hace Java.",
    "InicializadorDeArregloOpcional": "INSERTADO. El `{...}` opcional que el logro agrega.",
    "InicializadorDeArreglo": "Las llaves con los elementos. Sólo es alcanzable desde "
                              "`<Creacion>`, y eso es lo que impone la restricción del logro de "
                              "que valga únicamente bajo `new`.",
    "ListaElementosOpcional": "INSERTADO. El inicializador puede estar vacío: `new int[0]{}`.",
    "ListaElementos": "Al menos un elemento.",
    "RestoListaElementos": "INSERTADO. Cero o más `, elemento`.",
    "ElementoInicializador": "Un elemento es una expresión o, a su vez, otro inicializador: por "
                             "eso el inicializador es anidable. Las dos alternativas son "
                             "disjuntas porque una expresión nunca empieza con `{`.",

    # --- Argumentos actuales ---
    "ArgsActuales": "Los argumentos de una llamada, entre paréntesis.",
    "ListaExpsOpcional": "INSERTADO. La lista puede estar vacía: `m()`.",
    "ListaExps": "Al menos un argumento. Cada uno es una `<Expresion>` completa, así que una "
                 "lambda es un argumento válido: `f(x -> x)`.",
    "RestoListaExps": "INSERTADO por la eliminación de recursión a izquierda. Cero o más "
                      "`, expresión`.",
}


def secciones_del_archivo(texto):
    """El no terminal con que arranca cada sección, en el orden del `.g`."""
    orden, actual = [], []
    for linea in texto.split("\n"):
        limpia = linea.strip()
        if limpia.startswith("#"):
            continue
        if "->" in limpia:
            izquierda = limpia.split("->")[0].strip()
            if izquierda not in actual:
                actual.append(izquierda)
    return actual


def latex_de(texto):
    """Convierte el marcado liviano de las explicaciones a LaTeX."""
    texto = texto.replace("\\", r"\textbackslash{}")
    for viejo, nuevo in (("&", r"\&"), ("%", r"\%"), ("_", r"\_"), ("#", r"\#"),
                         ("$", r"\$"), ("{", r"\{"), ("}", r"\}")):
        texto = texto.replace(viejo, nuevo)
    texto = re.sub(r"`([^`]+)`", lambda m: r"\texttt{%s}" % m.group(1), texto)
    texto = texto.replace("INSERTADO", r"\textbf{Insertado}")
    return texto


def main():
    g = parsear(open(sys.argv[1], encoding="utf-8").read())
    nts = g.no_terminales
    orden = secciones_del_archivo(open(sys.argv[1], encoding="utf-8").read())
    inicio = {nt: i for i, (_, nt, _) in enumerate(SECCIONES)}

    faltan = [nt for nt in orden if nt not in EXPLICACION]
    if faltan:
        sys.exit("sin explicación: " + ", ".join(faltan))

    print(r"\documentclass[11pt,a4paper]{article}")
    print(r"\input{preambulo}")
    print(r"\begin{document}")
    print(CARATULA)

    seccion_actual = -1
    for nt in orden:
        if nt in inicio:
            seccion_actual = inicio[nt]
            titulo, _, intro = SECCIONES[seccion_actual]
            print(r"\section{%s}" % titulo)
            print(latex_de(intro))
            print()
        # Compacto a propósito: el nombre del no terminal va una sola vez como
        # encabezado corrido, las producciones debajo sin repetir el lado
        # izquierdo, y la explicación en cuerpo chico. Es un documento de
        # consulta; con \subsection por cada uno de los 112 ocupaba el doble.
        print(r"\par\medskip\noindent\textbf{\nt{%s}}\nobreak\par\nobreak" % nt)
        print(r"\begingroup\small\setlength{\leftskip}{1.1em}\raggedright")
        print(r"\hyphenpenalty=10000 \exhyphenpenalty=10000 \hbadness=10000")
        for cuerpo in g.por_izquierda[nt]:
            derecha = " ".join(render(s, nts) for s in cuerpo)
            print(r"\par\hangindent=1.6em\hangafter=1\noindent\deriva{} %s" % derecha)
        print(r"\par\endgroup")
        print(r"{\setlength{\leftskip}{1.1em}\noindent %s\par}" % latex_de(EXPLICACION[nt]))

    print(r"\end{document}")


CARATULA = r"""
\thispagestyle{empty}
\begin{center}
\vspace*{1.5cm}
{\Huge\bfseries La gramática, regla por regla\par}
\vspace{0.6cm}
{\large\color{tinta} Qué genera cada producción de \textsc{MiniJava} y por qué está escrita así\par}
\vspace{1.2cm}
\end{center}

\noindent
Documento de estudio, no de entrega. Recorre la gramática LL(1) final ---núcleo corregido más
todos los logros de la Etapa 1 y la Etapa 2--- no terminal por no terminal, en el mismo orden en
que está escrita.

\medskip\noindent
Las producciones \textbf{se generan} desde \texttt{compilador/gramatica/minijava-ll1-final.g}, así
que no pueden quedar viejas; lo único escrito a mano son las explicaciones. Para regenerarlo:

\begin{consola}
cd compilador
python3 herramientas/explicacion_de_reglas.py gramatica/minijava-ll1-final.g
\end{consola}

\medskip\noindent
Los no terminales marcados como \textbf{Insertado} no existen en la gramática de la cátedra:
aparecieron al eliminar la recursión a izquierda o al factorizar, y cada uno es un \emph{punto de
decisión} —el lugar donde el analizador elige con un token de anticipación—. Son los que el
analizador semántico va a querer tener identificados.

\vspace{0.8cm}
\hrule
\vspace{0.4cm}
\tableofcontents
\newpage
"""

if __name__ == "__main__":
    main()
