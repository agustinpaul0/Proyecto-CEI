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
InicializacionDeAtributo -> = ExpresionCondicionalOLambda           @+atrinit
InicializacionDeAtributo -> EPSILON

# <Metodo> sobrevive como no terminal propio y lo comparten la rama static y la
# rama void, así que el semántico tiene UN solo lugar donde construir el nodo de
# método para esas dos formas. La tercera forma (tipo de referencia) ya consumió
# el idMetVar al factorizar contra el atributo, y llega por AtributoOMetodo.
Metodo -> idMetVar CuerpoDeMetodo

# Se comparte a propósito: es la misma construcción en método, método static y
# constructor.
CuerpoDeMetodo -> ArgsFormales Bloque

# Los métodos de interfaz admiten `public` pero no `private`, que es lo que
# hace Java: verificado con javac, un método de interfaz `private` exige cuerpo
# («missing method body, or declare abstract»), y MiniJava no tiene forma de
# escribir ese cuerpo —<MetodoInterfaz> termina en `;` en la especificación
# original y ningún logro lo cambia—. O sea que un `private` acá sería siempre
# la forma que Java rechaza.
MetodoInterfaz -> PublicOpcional TipoMetodo idMetVar ArgsFormales ;
PublicOpcional -> public                                      @+visibilidad
PublicOpcional -> EPSILON

TipoMetodo -> Tipo | void

# ===========================================================================
# TIPOS
# ===========================================================================

Tipo -> TipoBase DimensionesOpcionales

TipoBase -> TipoPrimitivo | TipoReferencia | idGen

DimensionesOpcionales -> [ ] DimensionesOpcionales | EPSILON

TipoReferencia -> idClase TipoGenericoOpcional

# `float` es token del léxico desde la Etapa 1 pero no estaba en la
# gramática; sin esta alternativa el logro de Etapa 1 queda inutilizable.
TipoPrimitivo -> boolean | char | int
TipoPrimitivo -> float                                        @+float

# En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.
TipoGenericoOpcional -> < ArgumentoDeTipo > | EPSILON

# la recursión por <TipoGenericoOpcional> es la que habilita el
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
# Con el logro de variables locales clásicas una declaración local puede empezar con idClase (`C x;`), y
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
SentenciaExpresion -> ExpresionSinLambda                             @-varclasica

# Con el logro, `C x;` y `C.m();` compiten por idClase y hay que factorizarlo.
Sentencia -> ExpresionSinIdClase ;                            @+varclasica
Sentencia -> TipoPrimitivo DimensionesOpcionales ListaDeclaradores ;   @+varclasica
Sentencia -> idGen DimensionesOpcionales ListaDeclaradores ;  @+varclasica
Sentencia -> idClase SentenciaDesdeIdClase                    @+varclasica

SentenciaDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales ListaDeclaradores ;   @+varclasica
SentenciaDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ;           @+varclasica

VarLocal -> var idMetVar = ExpresionCondicionalOLambda

# Varias variables por sentencia, cada una con inicialización opcional:
# `int x,y,z = 10;`
ListaDeclaradores -> idMetVar InicializacionDeDeclarador RestoListaDeclaradores           @+varclasica
RestoListaDeclaradores -> , idMetVar InicializacionDeDeclarador RestoListaDeclaradores    @+varclasica
RestoListaDeclaradores -> EPSILON                                                         @+varclasica

InicializacionDeDeclarador -> = ExpresionCondicionalOLambda | EPSILON                            @+varclasica

Return -> return ExpresionOpcional

ExpresionOpcional -> Expresion | EPSILON

If -> if ( Expresion ) Sentencia ElseOpcional
ElseOpcional -> else Sentencia | EPSILON

While -> while ( Expresion ) Sentencia

# ===========================================================================
# Fors! E2
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
EncabezadoForTrasVar -> = ExpresionCondicionalOLambda ClausulasFinalesFor                        @+for

EncabezadoForTrasDeclarador -> : Expresion                                                @+for
EncabezadoForTrasDeclarador -> InicializacionDeDeclaradorFor ClausulasFinalesFor          @+for

InicializacionDeDeclaradorFor -> = ExpresionCondicionalOLambda | EPSILON                         @+for

# Una sola expresión por separador, como pide el logro.
ClausulasFinalesFor -> ; ExpresionOpcional ; ExpresionOpcional                            @+for

# ===========================================================================
# EXPRESIONES
# ===========================================================================
# Las expresiones se organizan en capas, de menor a mayor precedencia:
# <Expresion> es la asignación (no asociativa, como en la gramática original);
# <ExpresionCondicional> el ternario; <ExpresionCompuesta> los operadores
# binarios, en un solo nivel como en el original; <ExpresionBasica> los
# operadores unarios prefijos; y <Operando> un literal, o una referencia con
# posfijo opcional.
#
# La lambda NO es una capa de esta jerarquía ni un <Primario>: es una
# alternativa del nivel MÁS ALTO, igual que en el JLS, que define
# `Expression: LambdaExpression | AssignmentExpression`. Por eso `->` no tiene
# precedencia contra ningún operador —no compite con ellos, vive arriba de
# todos—, y `1 + x -> x`, `- x -> x` y `a == x -> x` son inválidos, como en
# Java. Las dos cabezas que una lambda comparte con una expresión normal,
# `idMetVar` y `(`, se factorizan en <ExpresionDesdePrimarioOLambda>.

# Todo lado derecho de un `=` —el de la asignación y el de los cinco
# inicializadores— es una <ExpresionCondicionalOLambda> y no una <Expresion>.
# Eso preserva lo que la regla original conseguía usando <ExpresionCompuesta>:
# que `a = b = c` no sea válido. Y como con los logros de ternario y de lambda
# apagados <ExpresionCondicionalOLambda> es un alias de <ExpresionCompuesta>, el
# lenguaje base queda idéntico al de la cátedra.
Expresion -> ExpresionCondicionalOLambda RestoExpresion

# La <Expresion> de la gramática original: la que NO admite una lambda en la
# raíz. Se usa en los dos lugares donde Java tampoco la admite: adentro de una
# expresión parentizada que después va a ser operando, y como sentencia-
# expresión (`x -> x;` es «not a statement» en Java).
ExpresionSinLambda -> ExpresionCondicional RestoExpresion

# El lado derecho es ExpresionCondicionalOLambda y NO Expresion: la gramática
# original pone <ExpresionCompuesta> ahí, o sea que `a = b = c` NO es válido.
# Admite lambda porque el JLS define `Assignment: LeftHandSide = Expression`.
RestoExpresion -> OperadorAsignacion ExpresionCondicionalOLambda | EPSILON

OperadorAsignacion -> =

# ---------------------------------------------------------------------------
# El nivel del JLS `LambdaExpression | ConditionalExpression`: admite lambda,
# no admite `=` en la raíz. Es el operando de los seis `=` y el tercer operando
# del ternario.
#
# Las cabezas que NO pueden empezar una lambda delegan en la jerarquía de
# siempre; las dos que sí —`idMetVar` y `(`— se factorizan. La cola se comparte
# con esa jerarquía, así que no hay lógica duplicada: sólo las cabezas.
ExpresionCondicionalOLambda -> OperadorUnario Operando ColaTrasOperandoSinAsignacion
ExpresionCondicionalOLambda -> Primitivo ColaTrasOperandoSinAsignacion
ExpresionCondicionalOLambda -> ExpresionDesdePrimarioOLambda

ExpresionDesdePrimarioOLambda -> idClase . idMetVar ArgsActuales ColaDesdePrimarioSinAsignacion
ExpresionDesdePrimarioOLambda -> this ColaDesdePrimarioSinAsignacion
ExpresionDesdePrimarioOLambda -> stringLiteral ColaDesdePrimarioSinAsignacion
ExpresionDesdePrimarioOLambda -> Creacion ColaDesdePrimarioSinAsignacion
ExpresionDesdePrimarioOLambda -> idMetVar AccesoLlamadaOLambda
ExpresionDesdePrimarioOLambda -> ParentizadaOLambda

ExpresionCondicional -> ExpresionCompuesta RestoCondicional

# Con el logro apagado <RestoCondicional> queda en solo ε, o sea que
# <ExpresionCondicional> se vuelve un alias de <ExpresionCompuesta> y la capa
# desaparece sin dejar rastro. Asociativo a derecha, como en Java.
#
# El operando del MEDIO es una <Expresion> y el de la derecha una
# <ExpresionCondicionalOLambda>, tal cual el JLS 15.25: en Java `a ? b = 1 : c`
# es válido, `a ? b : c = d` no lo es, y `a ? b : x -> x` sí —la producción del
# JLS es `? Expression : LambdaExpression`—. No rompe LL(1) porque
# Primeros(Expresion) no contiene `:`.
RestoCondicional -> ? Expresion : ExpresionCondicionalOLambda                             @+ternario
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

# floatLiteral
Primitivo -> true | false | intLiteral | charLiteral | null
Primitivo -> floatLiteral                                     @+float

# --- Expresión que no puede empezar con idClase ----------------------------
# Se usa donde una expresión compite con una declaración de variable local
# (sentencia y encabezado del for). Sólo duplica las *cabezas*: todas las colas
# se comparten con la jerarquía normal, así que no hay lógica repetida.
#
# NO admite lambda en la raíz, por la misma razón que <ExpresionSinLambda>: los
# dos contextos donde se usa son sentencia-expresión e inicialización del for,
# y en Java los dos exigen un StatementExpression, que nunca es una lambda.

ExpresionSinIdClase -> OperadorUnario Operando ColaTrasOperando
ExpresionSinIdClase -> Primitivo ColaTrasOperando
ExpresionSinIdClase -> PrimarioSinIdClase ColaDeExpresionDesdePrimario

# La cola se parte en dos para que el nivel <ExpresionCondicionalOLambda> pueda
# reusarla sin arrastrar el `=`, que ahí no corresponde.
ColaTrasOperando -> ColaTrasOperandoSinAsignacion RestoExpresion
ColaTrasOperandoSinAsignacion -> RestoExpresionCompuesta RestoCondicional

ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
ColaDesdePrimarioSinAsignacion -> RestoReferencia PosfijoOpcional ColaTrasOperandoSinAsignacion

# ===========================================================================
# REFERENCIAS Y PRIMARIOS
# ===========================================================================
# Ningún <Primario> puede ser una lambda: ésa es exactamente la corrección de
# precedencia. Un <Primario> es un operando, y una lambda no puede serlo.

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
PrimarioSinIdClase -> Parentizada
PrimarioSinIdClase -> AccesoVarOLlamada
PrimarioSinIdClase -> Creacion

# El contenido es una <ExpresionSinLambda>: `(x -> x)` como OPERANDO no existe
# en Java —`(x -> x) + 1` y `(x -> x).m()` son «lambda expression not expected
# here»—. La lambda parentizada sí es válida cuando ella misma es toda la
# expresión, y ese caso lo cubre <ParentizadaOLambda>.
Parentizada -> ( ExpresionSinLambda )

# Dedicado a este contexto: vacío = era <AccesoVar>, con argumentos = era
# <LlamadaMetodo>.
AccesoVarOLlamada -> idMetVar AccesoLlamada
AccesoLlamada -> ArgsActuales | EPSILON

# ===========================================================================
# MiniLambdas E2
# ===========================================================================
# Las dos cabezas ambiguas, resueltas en el nivel más alto de la jerarquía.
#
# `(` abre tanto una expresión parentizada como la lista de parámetros de una
# lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` vs `(a+b)`), pero
# NO lo necesita:
#   - `( )`      sólo puede ser una lambda de cero parámetros;
#   - `( e , …`  sólo puede ser una lambda: MiniJava no tiene operador coma;
#   - `( e )`    se decide con el token que sigue al `)`: `->` es lambda,
#                cualquier otro es una expresión parentizada.
# Con eso alcanza un token de lookahead en cada punto.

# Con flecha = lambda de un parámetro sin paréntesis; si no, es el <AccesoVar> o
# la <LlamadaMetodo> de siempre, con la cola de expresión que le corresponde.
AccesoLlamadaOLambda -> -> CuerpoLambda                                                   @+lambda
AccesoLlamadaOLambda -> AccesoLlamada ColaDesdePrimarioSinAsignacion

ParentizadaOLambda -> ( ContenidoDeParentesis

ContenidoDeParentesis -> ) -> CuerpoLambda                                                @+lambda
ContenidoDeParentesis -> Expresion RestoDeParentesis

RestoDeParentesis -> ) FinDeParentizadaOLambda
RestoDeParentesis -> , ListaParametrosLambda ) -> CuerpoLambda                            @+lambda

FinDeParentizadaOLambda -> -> CuerpoLambda                                                @+lambda
FinDeParentizadaOLambda -> ColaDesdePrimarioSinAsignacion

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

# el diamante `<>` sólo se admite acá, en la rama que va a
# ArgsActuales: `new C<>()` es válido y `new C<>[3]` no. La restricción del
# logro queda impuesta por la gramática, no delegada al semántico.
GenericoDeCreacion -> > ArgsActuales                                                      @+generico
GenericoDeCreacion -> ArgumentoDeTipo > ArregloOInstanciaDeClase

ArregloOInstanciaDeClase -> CreacionDeArreglo
ArregloOInstanciaDeClase -> ArgsActuales

# `new int[]{1,2,3}`. Sin el logro:
#   CreacionDeArreglo -> [ Expresion ] DimensionesConTamanioOpcional
CreacionDeArreglo -> [ RestoCreacionDeArreglo
RestoCreacionDeArreglo -> Expresion ] DimensionesConTamanioOpcional

# El logro dice «como en Java» y sólo restringe DÓNDE (dentro de un `new`), no
# cuántas dimensiones. Java admite `new int[][]{{1,2},{3,4}}`, así que el
# inicializador es anidable: un elemento puede ser una expresión o, a su vez,
# otro inicializador.
RestoCreacionDeArreglo -> ] DimensionesVaciasOpcional InicializadorDeArreglo              @+arrinit
DimensionesVaciasOpcional -> [ ] DimensionesVaciasOpcional | EPSILON                      @+arrinit
InicializadorDeArreglo -> { ListaElementosOpcional }                                      @+arrinit
ListaElementosOpcional -> ListaElementos | EPSILON                                        @+arrinit
ListaElementos -> ElementoInicializador RestoListaElementos                               @+arrinit
RestoListaElementos -> , ElementoInicializador RestoListaElementos | EPSILON              @+arrinit
ElementoInicializador -> Expresion | InicializadorDeArreglo                               @+arrinit

DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
DimensionesConTamanioOpcional -> DimensionesConTamanio | EPSILON

# ===========================================================================
# ARGUMENTOS ACTUALES
# ===========================================================================
# Sección propia y no parte de la creación: la lista de argumentos de una
# llamada la usan por igual el `new`, el encadenado, la llamada estática y el
# encabezado del for.

ArgsActuales -> ( ListaExpsOpcional )

ListaExpsOpcional -> ListaExps | EPSILON

ListaExps -> Expresion RestoListaExps
RestoListaExps -> , Expresion RestoListaExps | EPSILON
