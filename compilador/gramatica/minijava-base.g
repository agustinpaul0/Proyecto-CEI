# Gramática BASE de MiniJava, tal cual el PDF de la cátedra.
# Transcripción de documentacion/sintactico/gramatica-original-de-la-catedra.md
#
# NO es LL(1) — este archivo existe justamente para que el script lo demuestre:
#     python3 herramientas/primeros.py gramatica/minijava-base.g
#
# La versión corregida vive en minijava-ll1.g.

Inicial -> ListaClases eof

ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON

Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }

Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }

GenericidadOpcional -> < idGen > | EPSILON

HerenciaOpcional -> extends TipoReferencia | implements TipoReferencia | EPSILON

ExtensionOpcional -> extends TipoReferencia | EPSILON

ListaMiembros -> Miembro ListaMiembros | EPSILON

ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz | EPSILON

Miembro -> Atributo | Metodo | Constructor

Atributo -> Tipo idMetVar ;

Metodo -> ModificadorOpcional TipoMetodo idMetVar ArgsFormales Bloque

MetodoInterfaz -> TipoMetodo idMetVar ArgsFormales ;

Constructor -> public idClase ArgsFormales Bloque

ModificadorOpcional -> static | EPSILON

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

ListaArgsFormales -> ArgFormal
ListaArgsFormales -> ListaArgsFormales , ArgFormal

ArgFormal -> Tipo idMetVar

Bloque -> { ListaSentencias }

ListaSentencias -> Sentencia ListaSentencias | EPSILON

Sentencia -> ;
Sentencia -> Asignacion ;
Sentencia -> Llamada ;
Sentencia -> VarLocal ;
Sentencia -> Return ;
Sentencia -> If
Sentencia -> While
Sentencia -> Bloque

Asignacion -> Expresion

Llamada -> Expresion

VarLocal -> var idMetVar = ExpresionCompuesta

Return -> return ExpresionOpcional

ExpresionOpcional -> Expresion | EPSILON

If -> if ( Expresion ) Sentencia
If -> if ( Expresion ) Sentencia else Sentencia

While -> while ( Expresion ) Sentencia

Expresion -> ExpresionCompuesta OperadorAsignacion ExpresionCompuesta
Expresion -> ExpresionCompuesta

OperadorAsignacion -> =

ExpresionCompuesta -> ExpresionCompuesta OperadorBinario ExpresionBasica
ExpresionCompuesta -> ExpresionBasica

OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %

ExpresionBasica -> OperadorUnario Operando
ExpresionBasica -> Operando

OperadorUnario -> + | - | !

Operando -> Primitivo
Operando -> Referencia

Primitivo -> true | false | intLiteral | charLiteral | null

Referencia -> Primario
Referencia -> Referencia VarEncadenada
Referencia -> Referencia MetodoEncadenado
Referencia -> Referencia AccesoArreglo

Primario -> this
Primario -> stringLiteral
Primario -> AccesoVar
Primario -> CrearArreglo
Primario -> LlamadaConstructor
Primario -> LlamadaMetodo
Primario -> LlamadaMetodoEstatico
Primario -> ExpresionParentizada

AccesoVar -> idMetVar

CrearArreglo -> new TipoBase DimensionesConTamanio

LlamadaConstructor -> new TipoReferencia ArgsActuales

ExpresionParentizada -> ( Expresion )

LlamadaMetodo -> idMetVar ArgsActuales

LlamadaMetodoEstatico -> idClase . idMetVar ArgsActuales

DimensionesConTamanio -> [ Expresion ] DimensionesConTamanio | [ Expresion ]

ArgsActuales -> ( ListaExpsOpcional )

ListaExpsOpcional -> ListaExps | EPSILON

ListaExps -> Expresion
ListaExps -> Expresion , ListaExps

VarEncadenada -> . idMetVar

MetodoEncadenado -> . idMetVar ArgsActuales

AccesoArreglo -> [ Expresion ]
