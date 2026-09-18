# Gramática LL(1) de MiniJava — NÚCLEO (sin logros)
#
# Derivada de minijava-base.g siguiendo Clase 4: primero eliminar recursión a
# izquierda, después factorizar (slide 37). El paso a paso atómico de cada
# transformación está en documentacion/transformacion-gramatica.md.
#
#     python3 herramientas/primeros.py gramatica/minijava-ll1.g
#
# Las extensiones de los logros viven en minijava-ll1-final.g.

Inicial -> ListaClases eof

ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON

Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }

Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }

GenericidadOpcional -> < idGen > | EPSILON

HerenciaOpcional -> extends TipoReferencia | implements TipoReferencia | EPSILON

ExtensionOpcional -> extends TipoReferencia | EPSILON

ListaMiembros -> Miembro ListaMiembros | EPSILON

ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz | EPSILON

# --- F6: factorización profunda de Miembro ---------------------------------
# Atributo y Metodo comparten el prefijo <Tipo> idMetVar; se suben ambos hasta
# Miembro y se factoriza. Constructor sobrevive como NT propio porque `public`
# lo distingue solo.
Miembro -> Tipo idMetVar AtributoOMetodo
Miembro -> static TipoMetodo Metodo
Miembro -> void Metodo
Miembro -> Constructor

# El NT insertado ES el punto de decisión: acá el semántico sabe si construye
# un nodo de atributo o uno de método.
AtributoOMetodo -> ;                 # cola de <Atributo>
AtributoOMetodo -> CuerpoDeMetodo    # cola de <Metodo>

# <Metodo> sobrevive como NT propio, compartido por la rama static y la void.
Metodo -> idMetVar CuerpoDeMetodo

# <CuerpoDeMetodo> se comparte a propósito: es literalmente la misma
# construcción en método, constructor y método con static.
CuerpoDeMetodo -> ArgsFormales Bloque

Constructor -> public idClase CuerpoDeMetodo

MetodoInterfaz -> TipoMetodo idMetVar ArgsFormales ;

TipoMetodo -> Tipo | void

Tipo -> TipoBase DimensionesOpcionales

TipoBase -> TipoPrimitivo | TipoReferencia | idGen

DimensionesOpcionales -> [ ] DimensionesOpcionales | EPSILON

TipoReferencia -> idClase TipoGenericoOpcional

TipoPrimitivo -> boolean | char | int

TipoGenericoOpcional -> < InstanciadoOParametrico > | EPSILON

InstanciadoOParametrico -> idGen | idClase

ArgsFormales -> ( ListaArgsFormalesOpcional )

ListaArgsFormalesOpcional -> ListaArgsFormales | EPSILON

# --- R1: eliminación de recursión a izquierda en ListaArgsFormales ---------
ListaArgsFormales -> ArgFormal RestoListaArgsFormales
RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales | EPSILON

ArgFormal -> Tipo idMetVar

Bloque -> { ListaSentencias }

ListaSentencias -> Sentencia ListaSentencias | EPSILON

# --- F2: Asignacion y Llamada eran la MISMA producción (ambigüedad) --------
# Se colapsan en una única sentencia-expresión; distinguir asignación de
# llamada es trabajo del semántico, no del sintáctico.
Sentencia -> ;
Sentencia -> SentenciaExpresion ;
Sentencia -> VarLocal ;
Sentencia -> Return ;
Sentencia -> If
Sentencia -> While
Sentencia -> Bloque

SentenciaExpresion -> Expresion

VarLocal -> var idMetVar = ExpresionCompuesta

Return -> return ExpresionOpcional

ExpresionOpcional -> Expresion | EPSILON

# --- F3: factorización de If -----------------------------------------------
# Los Primeros quedan disjuntos, pero el *dangling else* persiste como
# ambigüedad y se resuelve con una regla de implementación (Clase 4, slide 49):
# el esquema simple toma la rama `else` apenas la ve, o sea que el else liga al
# if más interno — la regla de Java.
If -> if ( Expresion ) Sentencia ElseOpcional
ElseOpcional -> else Sentencia | EPSILON

While -> while ( Expresion ) Sentencia

# --- F1: factorización de Expresion ----------------------------------------
Expresion -> ExpresionCompuesta RestoExpresion
RestoExpresion -> OperadorAsignacion ExpresionCompuesta | EPSILON

OperadorAsignacion -> =

# --- R2: eliminación de recursión a izquierda en ExpresionCompuesta --------
ExpresionCompuesta -> ExpresionBasica RestoExpresionCompuesta
RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta | EPSILON

OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %

ExpresionBasica -> OperadorUnario Operando
ExpresionBasica -> Operando

OperadorUnario -> + | - | !

Operando -> Primitivo
Operando -> Referencia

Primitivo -> true | false | intLiteral | charLiteral | null

# --- R3: eliminación de recursión a izquierda en Referencia ----------------
# Eliminar la recursión deja VarEncadenada y MetodoEncadenado compartiendo el
# prefijo `.` (Clase 4, slide 38: eliminar recursión genera producciones sin
# factorizar). Se fusionan en Encadenado: sintácticamente son indistinguibles
# hasta ver si sigue un `(`.
Referencia -> Primario RestoReferencia
RestoReferencia -> Encadenado RestoReferencia
RestoReferencia -> AccesoArreglo RestoReferencia
RestoReferencia -> EPSILON

Encadenado -> . idMetVar EncadenadoAtributoOMetodo

# Dedicado a este contexto: vacío = era <VarEncadenada> (atributo encadenado),
# con argumentos = era <MetodoEncadenado>.
EncadenadoAtributoOMetodo -> ArgsActuales | EPSILON

# --- F7 y F8: factorización de Primario ------------------------------------
Primario -> this
Primario -> stringLiteral
Primario -> ExpresionParentizada
Primario -> AccesoVarOLlamadaMetodo
Primario -> LlamadaMetodoEstatico
Primario -> Creacion

ExpresionParentizada -> ( Expresion )

# F7: AccesoVar (idMetVar) y LlamadaMetodo (idMetVar ArgsActuales) comparten
# todo el prefijo. Mismo criterio que Encadenado.
AccesoVarOLlamadaMetodo -> idMetVar AccesoOLlamada

# Dedicado a este contexto: vacío = era <AccesoVar>, con argumentos = era
# <LlamadaMetodo>. NO se reusa EncadenadoAtributoOMetodo aunque la forma
# coincida: son dos decisiones semánticas distintas.
AccesoOLlamada -> ArgsActuales | EPSILON

LlamadaMetodoEstatico -> idClase . idMetVar ArgsActuales

# F8: CrearArreglo (new TipoBase ...) y LlamadaConstructor (new TipoReferencia
# ...) comparten `new`, y TipoBase puede derivar a TipoReferencia: la colisión
# es a dos niveles. Se decide por lo que sigue al tipo — `[` es arreglo, `(` es
# constructor.
Creacion -> new ArregloOInstancia

ArregloOInstancia -> TipoPrimitivo DimensionesConTamanio
ArregloOInstancia -> idGen DimensionesConTamanio
ArregloOInstancia -> idClase TipoGenericoOpcional ArregloOInstanciaDeClase

# El NT insertado es el punto de decisión entre las dos construcciones.
ArregloOInstanciaDeClase -> DimensionesConTamanio   # era <CrearArreglo>
ArregloOInstanciaDeClase -> ArgsActuales            # era <LlamadaConstructor>

# --- F4: factorización de DimensionesConTamanio ----------------------------
# La original exige al menos una dimensión, así que la parte repetible va en un
# NT opcional aparte en vez de volverse `... | EPSILON`.
DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
DimensionesConTamanioOpcional -> DimensionesConTamanio | EPSILON

ArgsActuales -> ( ListaExpsOpcional )

ListaExpsOpcional -> ListaExps | EPSILON

# --- F5: factorización de ListaExps ----------------------------------------
ListaExps -> Expresion RestoListaExps
RestoListaExps -> , Expresion RestoListaExps | EPSILON

AccesoArreglo -> [ Expresion ]
