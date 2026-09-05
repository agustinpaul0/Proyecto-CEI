# Gramática LL(1) FINAL de MiniJava — núcleo + los 9 logros de Etapa 2 + float (Etapa 1)
#
#     python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g
#
# Cada producción que existe por un logro está marcada con [L:nombre]. Con el
# logro apagado se quita esa producción y la gramática debe seguir siendo LL(1):
# por eso ningún logro se apoya en producciones de otro.
#
# Logros:
#   [L:visibilidad]  Visibilidad Mejorada E2
#   [L:atrinit]      Atributos Inicializados
#   [L:varclasica]   Variables locales Clásicas E2
#   [L:for]          Fors! E2
#   [L:ternario]     Operador Ternario E2
#   [L:posfijo]      Operadores Posifijos E2
#   [L:lambda]       MiniLambdas E2
#   [L:arrinit]      Inicializacion de Arreglos E2
#   [L:generico]     Genericidad Avanzada E2
#   [L:float]        Floats (carry-over de Etapa 1)

Inicial -> ListaClases eof

ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON

Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }

Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }

# Una Clase o Interfaz sigue limitada a UN único parámetro de tipo, también con
# el logro de genericidad avanzada.
GenericidadOpcional -> < idGen > | EPSILON

HerenciaOpcional -> extends TipoReferencia | implements TipoReferencia | EPSILON

ExtensionOpcional -> extends TipoReferencia | EPSILON

ListaMiembros -> Miembro ListaMiembros | EPSILON

ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz | EPSILON

# ===========================================================================
# MIEMBROS
# ===========================================================================
# La visibilidad se factoriza al frente de los tres tipos de miembro. Eso hace
# que el `public` del <Constructor> original quede absorbido por
# <VisibilidadOpcional>, y que <Constructor> pase a competir con <Tipo> por el
# token idClase: `C(){}` (constructor) vs `C x;` (atributo de tipo C).

Miembro -> VisibilidadOpcional MiembroSinVisibilidad          @+visibilidad
Miembro -> MiembroSinVisibilidad                              @-visibilidad
Miembro -> Constructor                                        @-visibilidad

VisibilidadOpcional -> public | private | EPSILON             @+visibilidad

# Sin el logro, el `public` sólo aparece en el constructor y es lo que lo
# distingue del resto de los miembros: es la regla original de la cátedra.
Constructor -> public idClase CuerpoDeMetodo                  @-visibilidad

MiembroSinVisibilidad -> static TipoMetodo Metodo
MiembroSinVisibilidad -> void Metodo
MiembroSinVisibilidad -> TipoPrimitivo DimensionesOpcionales idMetVar AtributoOMetodo
MiembroSinVisibilidad -> idGen DimensionesOpcionales idMetVar AtributoOMetodo
MiembroSinVisibilidad -> idClase MiembroDesdeIdClase

# El NT insertado que decide entre constructor y miembro con tipo de referencia.
# Con visibilidad el constructor ya no lleva `public` propio, así que compite
# con el atributo/método de tipo referencia por el token idClase.
MiembroDesdeIdClase -> CuerpoDeMetodo                         @+visibilidad
MiembroDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar AtributoOMetodo

# El NT insertado que decide entre atributo y método.
AtributoOMetodo -> InicializacionDeAtributo ;
AtributoOMetodo -> CuerpoDeMetodo

# NT dedicado: la inicialización de un ATRIBUTO es un logro distinto que la de
# una variable local, y tienen que poder prenderse por separado.
InicializacionDeAtributo -> = ExpresionCompuesta               @+atrinit
InicializacionDeAtributo -> EPSILON

# <Metodo> sobrevive como no terminal propio y lo comparten la rama static y la
# rama void, así que el semántico tiene UN solo lugar donde construir el nodo de
# método para esas dos formas. La tercera forma (tipo de referencia) ya consumió
# el idMetVar al factorizar contra el atributo, y llega por AtributoOMetodo.
Metodo -> idMetVar CuerpoDeMetodo

# Se comparte a propósito: es la misma construcción en método, método static y
# constructor.
CuerpoDeMetodo -> ArgsFormales Bloque

# Los métodos de interfaz no llevan visibilidad: el logro habla de Métodos,
# Atributos y Constructores, que son los <Miembro> de una clase.
MetodoInterfaz -> TipoMetodo idMetVar ArgsFormales ;

TipoMetodo -> Tipo | void

# ===========================================================================
# TIPOS
# ===========================================================================

Tipo -> TipoBase DimensionesOpcionales

TipoBase -> TipoPrimitivo | TipoReferencia | idGen

DimensionesOpcionales -> [ ] DimensionesOpcionales | EPSILON

TipoReferencia -> idClase TipoGenericoOpcional

# [L:float] — `float` es token del léxico desde la Etapa 1 pero no estaba en la
# gramática; sin esta alternativa el logro de Etapa 1 queda inutilizable.
TipoPrimitivo -> boolean | char | int
TipoPrimitivo -> float                                        @+float

# En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.
TipoGenericoOpcional -> < ArgumentoDeTipo > | EPSILON

# [L:generico] — la recursión por TipoGenericoOpcional es la que habilita el
# anidamiento `List<List<Persona>>`. Sin el logro: `ArgumentoDeTipo -> idGen | idClase`.
ArgumentoDeTipo -> idGen
ArgumentoDeTipo -> idClase TipoGenericoOpcional               @+generico
ArgumentoDeTipo -> idClase                                    @-generico

ArgsFormales -> ( ListaArgsFormalesOpcional )

ListaArgsFormalesOpcional -> ListaArgsFormales | EPSILON

ListaArgsFormales -> ArgFormal RestoListaArgsFormales
RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales | EPSILON

ArgFormal -> Tipo idMetVar

# ===========================================================================
# SENTENCIAS
# ===========================================================================
# Con [L:varclasica] una declaración local puede empezar con idClase (`C x;`), y
# una sentencia-expresión también (`C.m();`). Se factoriza ese idClase y el
# desempate lo hace el token siguiente: `.` es expresión, cualquier otro es
# declaración. La rama de expresión reusa <ColaDeExpresionDesdePrimario> para no
# duplicar la jerarquía de expresiones.

Bloque -> { ListaSentencias }

ListaSentencias -> Sentencia ListaSentencias | EPSILON

Sentencia -> ;
Sentencia -> VarLocal ;
Sentencia -> Return ;
Sentencia -> If
Sentencia -> While
Sentencia -> Bloque
Sentencia -> For                                              @+for

# Sin variables locales clásicas, una sentencia-expresión puede empezar con
# idClase sin competir con nada: es la regla original.
Sentencia -> SentenciaExpresion ;                             @-varclasica
SentenciaExpresion -> Expresion                               @-varclasica

# Con el logro, `C x;` y `C.m();` compiten por idClase y hay que factorizarlo.
Sentencia -> ExpresionSinIdClase ;                            @+varclasica
Sentencia -> TipoPrimitivo DimensionesOpcionales ListaDeclaradores ;   @+varclasica
Sentencia -> idGen DimensionesOpcionales ListaDeclaradores ;  @+varclasica
Sentencia -> idClase SentenciaDesdeIdClase                    @+varclasica

SentenciaDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales ListaDeclaradores ;   @+varclasica
SentenciaDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ;           @+varclasica

VarLocal -> var idMetVar = ExpresionCompuesta

# Varias variables por sentencia, cada una con inicialización opcional:
# `int x,y,z = 10;`
ListaDeclaradores -> idMetVar InicializacionDeDeclarador RestoListaDeclaradores           @+varclasica
RestoListaDeclaradores -> , idMetVar InicializacionDeDeclarador RestoListaDeclaradores    @+varclasica
RestoListaDeclaradores -> EPSILON                                                         @+varclasica

InicializacionDeDeclarador -> = ExpresionCompuesta | EPSILON                              @+varclasica

Return -> return ExpresionOpcional

ExpresionOpcional -> Expresion | EPSILON

If -> if ( Expresion ) Sentencia ElseOpcional
ElseOpcional -> else Sentencia | EPSILON

While -> while ( Expresion ) Sentencia

# ===========================================================================
# [L:for] — Fors! E2
# ===========================================================================
# Una sola sentencia/expresión por separador. El `:` distingue el for de
# iteradores del estándar, y aparece justo después del declarador, así que el
# desempate es de un solo token.

For -> for ( EncabezadoFor ) Sentencia                                                    @+for

EncabezadoFor -> ; ExpresionOpcional ; ExpresionOpcional                                  @+for
EncabezadoFor -> var idMetVar EncabezadoForTrasVar                                        @+for
EncabezadoFor -> TipoPrimitivo DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador @+for
EncabezadoFor -> idGen DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador         @+for
EncabezadoFor -> idClase EncabezadoForDesdeIdClase                                        @+for
EncabezadoFor -> ExpresionSinIdClase ClausulasFinalesFor                                  @+for

EncabezadoForDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador   @+for
EncabezadoForDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ClausulasFinalesFor          @+for

# El `:` distingue el for de iteradores del estándar, y está a un solo token.
EncabezadoForTrasVar -> : Expresion                                                       @+for
EncabezadoForTrasVar -> = ExpresionCompuesta ClausulasFinalesFor                          @+for

EncabezadoForTrasDeclarador -> : Expresion                                                @+for
EncabezadoForTrasDeclarador -> InicializacionDeDeclaradorFor ClausulasFinalesFor          @+for

InicializacionDeDeclaradorFor -> = ExpresionCompuesta | EPSILON                           @+for

# Una sola expresión por separador, como pide el logro.
ClausulasFinalesFor -> ; ExpresionOpcional ; ExpresionOpcional                            @+for

# ===========================================================================
# EXPRESIONES
# ===========================================================================
# Capas, de menor a mayor precedencia:
#   Expresion            asignación (NO asociativa, como en la gramática original)
#   ExpresionCondicional ternario [L:ternario]
#   ExpresionCompuesta   operadores binarios (un solo nivel, como el original)
#   ExpresionBasica      operadores unarios prefijos
#   Operando             literal, o referencia con posfijo opcional [L:posfijo]

Expresion -> ExpresionCondicional RestoExpresion

# El lado derecho es ExpresionCondicional y NO Expresion: la gramática original
# pone <ExpresionCompuesta> ahí, o sea que `a = b = c` NO es válido en MiniJava.
RestoExpresion -> OperadorAsignacion ExpresionCondicional | EPSILON

OperadorAsignacion -> =

ExpresionCondicional -> ExpresionCompuesta RestoCondicional

# Con el logro apagado <RestoCondicional> queda en solo ε, o sea que
# <ExpresionCondicional> se vuelve un alias de <ExpresionCompuesta> y la capa
# desaparece sin dejar rastro. Asociativo a derecha, como en Java.
RestoCondicional -> ? ExpresionCondicional : ExpresionCondicional                         @+ternario
RestoCondicional -> EPSILON

ExpresionCompuesta -> ExpresionBasica RestoExpresionCompuesta
RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta | EPSILON

OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %

ExpresionBasica -> OperadorUnario Operando
ExpresionBasica -> Operando

OperadorUnario -> + | - | !

Operando -> Primitivo
Operando -> Referencia PosfijoOpcional

# Va sobre <Referencia> y no sobre <Operando>: `5++` no tiene sentido.
PosfijoOpcional -> ++                                                                     @+posfijo
PosfijoOpcional -> --                                                                     @+posfijo
PosfijoOpcional -> EPSILON

# [L:float] — floatLiteral
Primitivo -> true | false | intLiteral | charLiteral | null
Primitivo -> floatLiteral                                     @+float

# --- Expresión que no puede empezar con idClase ----------------------------
# Se usa donde una expresión compite con una declaración de variable local
# (sentencia y encabezado del for). Sólo duplica las *cabezas*: todas las colas
# se comparten con la jerarquía normal, así que no hay lógica repetida.

ExpresionSinIdClase -> OperadorUnario Operando ColaTrasOperando
ExpresionSinIdClase -> Primitivo ColaTrasOperando
ExpresionSinIdClase -> PrimarioSinIdClase ColaDeExpresionDesdePrimario

ColaTrasOperando -> RestoExpresionCompuesta RestoCondicional RestoExpresion

ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando

# ===========================================================================
# REFERENCIAS Y PRIMARIOS
# ===========================================================================

Referencia -> Primario RestoReferencia

RestoReferencia -> Encadenado RestoReferencia
RestoReferencia -> AccesoArreglo RestoReferencia
RestoReferencia -> EPSILON

Encadenado -> . idMetVar EncadenadoAtributoOMetodo

# Dedicado a este contexto: vacío = era <VarEncadenada>, con argumentos = era
# <MetodoEncadenado>.
EncadenadoAtributoOMetodo -> ArgsActuales | EPSILON

AccesoArreglo -> [ Expresion ]

Primario -> LlamadaMetodoEstatico
Primario -> PrimarioSinIdClase

LlamadaMetodoEstatico -> idClase . idMetVar ArgsActuales

PrimarioSinIdClase -> this
PrimarioSinIdClase -> stringLiteral
PrimarioSinIdClase -> ParentizadaOLambda
PrimarioSinIdClase -> AccesoVarLlamadaOLambda
PrimarioSinIdClase -> Creacion

# Dedicado a este contexto: vacío = era <AccesoVar>, con argumentos = era
# <LlamadaMetodo>, con flecha = lambda de un parámetro sin paréntesis.
AccesoVarLlamadaOLambda -> idMetVar AccesoLlamadaOLambda
AccesoLlamadaOLambda -> ArgsActuales
AccesoLlamadaOLambda -> -> CuerpoLambda                                                   @+lambda
AccesoLlamadaOLambda -> EPSILON

# ===========================================================================
# [L:lambda] — MiniLambdas E2
# ===========================================================================
# `(` abre tanto una expresión parentizada como la lista de parámetros de una
# lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` vs `(a+b)`), pero
# NO lo necesita:
#   - `( )`      sólo puede ser una lambda de cero parámetros;
#   - `( e , …`  sólo puede ser una lambda: MiniJava no tiene operador coma;
#   - `( e )`    se decide con el token que sigue al `)`: `->` es lambda,
#                cualquier otro es una expresión parentizada.
# Con eso alcanza un token de lookahead en cada punto.

ParentizadaOLambda -> ( ContenidoDeParentesis

ContenidoDeParentesis -> ) -> CuerpoLambda                                                @+lambda
ContenidoDeParentesis -> Expresion RestoDeParentesis

RestoDeParentesis -> ) FinDeParentizadaOLambda
RestoDeParentesis -> , ListaParametrosLambda ) -> CuerpoLambda                            @+lambda

FinDeParentizadaOLambda -> -> CuerpoLambda                                                @+lambda
FinDeParentizadaOLambda -> EPSILON

ListaParametrosLambda -> idMetVar RestoListaParametrosLambda                              @+lambda
RestoListaParametrosLambda -> , idMetVar RestoListaParametrosLambda | EPSILON             @+lambda

# Una única expresión, sin llaves.
CuerpoLambda -> Expresion                                                                 @+lambda

# ===========================================================================
# CREACIÓN
# ===========================================================================

Creacion -> new ArregloOInstancia

ArregloOInstancia -> TipoPrimitivo CreacionDeArreglo
ArregloOInstancia -> idGen CreacionDeArreglo
ArregloOInstancia -> idClase CreacionDesdeIdClase

CreacionDesdeIdClase -> < GenericoDeCreacion
CreacionDesdeIdClase -> CreacionDeArreglo
CreacionDesdeIdClase -> ArgsActuales

# [L:generico] — el diamante `<>` sólo se admite acá, en la rama que va a
# ArgsActuales: `new C<>()` es válido y `new C<>[3]` no. La restricción del
# logro queda impuesta por la gramática, no delegada al semántico.
GenericoDeCreacion -> > ArgsActuales                                                      @+generico
GenericoDeCreacion -> ArgumentoDeTipo > ArregloOInstanciaDeClase

ArregloOInstanciaDeClase -> CreacionDeArreglo
ArregloOInstanciaDeClase -> ArgsActuales

# [L:arrinit] — `new int[]{1,2,3}`. Sin el logro:
#   CreacionDeArreglo -> [ Expresion ] DimensionesConTamanioOpcional
CreacionDeArreglo -> [ RestoCreacionDeArreglo
RestoCreacionDeArreglo -> Expresion ] DimensionesConTamanioOpcional
RestoCreacionDeArreglo -> ] { ListaExpsOpcional }                                         @+arrinit

DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
DimensionesConTamanioOpcional -> DimensionesConTamanio | EPSILON

ArgsActuales -> ( ListaExpsOpcional )

ListaExpsOpcional -> ListaExps | EPSILON

ListaExps -> Expresion RestoListaExps
RestoListaExps -> , Expresion RestoListaExps | EPSILON
