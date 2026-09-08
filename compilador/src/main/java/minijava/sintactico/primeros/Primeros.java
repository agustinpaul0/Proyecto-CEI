package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-logros.g. No editar a mano: se regenera.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
// Las producciones que existen por un logro se emiten igual: el analizador
// consulta el logro por su cuenta antes de tomar la rama.
public final class Primeros {

    // Inicial -> ListaClases eof
    public static final ConjuntoDePrimeros INICIAL_LISTA_CLASES = de(
            TipoDeToken.PR_CLASS, TipoDeToken.EOF, TipoDeToken.PR_INTERFACE);

    // ListaClases -> Clase ListaClases
    public static final ConjuntoDePrimeros LISTA_CLASES_CLASE = de(TipoDeToken.PR_CLASS);

    // ListaClases -> Interfaz ListaClases
    public static final ConjuntoDePrimeros LISTA_CLASES_INTERFAZ = de(TipoDeToken.PR_INTERFACE);

    // ListaClases -> EPSILON
    public static final ConjuntoDePrimeros LISTA_CLASES = de();

    // Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }
    public static final ConjuntoDePrimeros CLASE_CLASS = de(TipoDeToken.PR_CLASS);

    // Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }
    public static final ConjuntoDePrimeros INTERFAZ_INTERFACE = de(TipoDeToken.PR_INTERFACE);

    // GenericidadOpcional -> < idGen >
    public static final ConjuntoDePrimeros GENERICIDAD_OPCIONAL_MENOR = de(TipoDeToken.OP_MENOR);

    // GenericidadOpcional -> EPSILON
    public static final ConjuntoDePrimeros GENERICIDAD_OPCIONAL = de();

    // HerenciaOpcional -> extends TipoReferencia
    public static final ConjuntoDePrimeros HERENCIA_OPCIONAL_EXTENDS = de(TipoDeToken.PR_EXTENDS);

    // HerenciaOpcional -> implements TipoReferencia
    public static final ConjuntoDePrimeros HERENCIA_OPCIONAL_IMPLEMENTS = de(TipoDeToken.PR_IMPLEMENTS);

    // HerenciaOpcional -> EPSILON
    public static final ConjuntoDePrimeros HERENCIA_OPCIONAL = de();

    // ExtensionOpcional -> extends TipoReferencia
    public static final ConjuntoDePrimeros EXTENSION_OPCIONAL_EXTENDS = de(TipoDeToken.PR_EXTENDS);

    // ExtensionOpcional -> EPSILON
    public static final ConjuntoDePrimeros EXTENSION_OPCIONAL = de();

    // ListaMiembros -> Miembro ListaMiembros
    public static final ConjuntoDePrimeros LISTA_MIEMBROS_MIEMBRO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PRIVATE, TipoDeToken.PR_PUBLIC,
            TipoDeToken.PR_STATIC, TipoDeToken.PR_VOID);

    // ListaMiembros -> EPSILON
    public static final ConjuntoDePrimeros LISTA_MIEMBROS = de();

    // ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz
    public static final ConjuntoDePrimeros LISTA_METODOS_INTERFAZ_METODO_INTERFAZ = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PUBLIC, TipoDeToken.PR_VOID);

    // ListaMetodosInterfaz -> EPSILON
    public static final ConjuntoDePrimeros LISTA_METODOS_INTERFAZ = de();

    // Miembro -> VisibilidadOpcional MiembroSinVisibilidad
    public static final ConjuntoDePrimeros MIEMBRO_VISIBILIDAD_OPCIONAL = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PRIVATE, TipoDeToken.PR_PUBLIC,
            TipoDeToken.PR_STATIC, TipoDeToken.PR_VOID);

    // Miembro -> MiembroSinVisibilidad
    public static final ConjuntoDePrimeros MIEMBRO_MIEMBRO_SIN_VISIBILIDAD = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_STATIC, TipoDeToken.PR_VOID);

    // Miembro -> Constructor
    public static final ConjuntoDePrimeros MIEMBRO_CONSTRUCTOR = de(TipoDeToken.PR_PUBLIC);

    // VisibilidadOpcional -> public
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // VisibilidadOpcional -> private
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL_PRIVATE = de(TipoDeToken.PR_PRIVATE);

    // VisibilidadOpcional -> EPSILON
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL = de();

    // Constructor -> public idClase CuerpoDeMetodo
    public static final ConjuntoDePrimeros CONSTRUCTOR_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // MiembroSinVisibilidad -> static TipoMetodo Metodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_STATIC = de(TipoDeToken.PR_STATIC);

    // MiembroSinVisibilidad -> void Metodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_VOID = de(TipoDeToken.PR_VOID);

    // MiembroSinVisibilidad -> TipoPrimitivo DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // MiembroSinVisibilidad -> idGen DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_ID_GEN = de(TipoDeToken.ID_GEN);

    // MiembroSinVisibilidad -> idClase MiembroDesdeIdClase
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // MiembroDesdeIdClase -> CuerpoDeMetodo
    public static final ConjuntoDePrimeros MIEMBRO_DESDE_ID_CLASE_CUERPO_DE_METODO = de(TipoDeToken.PAREN_A);

    // MiembroDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL = de(
            TipoDeToken.OP_MENOR, TipoDeToken.CORCHETE_A, TipoDeToken.ID_MV);

    // AtributoOMetodo -> InicializacionDeAtributo ;
    public static final ConjuntoDePrimeros ATRIBUTO_OMETODO_INICIALIZACION_DE_ATRIBUTO = de(
            TipoDeToken.PUNTO_Y_COMA, TipoDeToken.OP_ASIGNACION);

    // AtributoOMetodo -> CuerpoDeMetodo
    public static final ConjuntoDePrimeros ATRIBUTO_OMETODO_CUERPO_DE_METODO = de(TipoDeToken.PAREN_A);

    // InicializacionDeAtributo -> = ExpresionCondicional
    public static final ConjuntoDePrimeros INICIALIZACION_DE_ATRIBUTO_ASIGNACION = de(
            TipoDeToken.OP_ASIGNACION);

    // InicializacionDeAtributo -> EPSILON
    public static final ConjuntoDePrimeros INICIALIZACION_DE_ATRIBUTO = de();

    // Metodo -> idMetVar CuerpoDeMetodo
    public static final ConjuntoDePrimeros METODO_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // CuerpoDeMetodo -> ArgsFormales Bloque
    public static final ConjuntoDePrimeros CUERPO_DE_METODO_ARGS_FORMALES = de(TipoDeToken.PAREN_A);

    // MetodoInterfaz -> PublicOpcional TipoMetodo idMetVar ArgsFormales ;
    public static final ConjuntoDePrimeros METODO_INTERFAZ_PUBLIC_OPCIONAL = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PUBLIC, TipoDeToken.PR_VOID);

    // PublicOpcional -> public
    public static final ConjuntoDePrimeros PUBLIC_OPCIONAL_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // PublicOpcional -> EPSILON
    public static final ConjuntoDePrimeros PUBLIC_OPCIONAL = de();

    // TipoMetodo -> Tipo
    public static final ConjuntoDePrimeros TIPO_METODO_TIPO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT);

    // TipoMetodo -> void
    public static final ConjuntoDePrimeros TIPO_METODO_VOID = de(TipoDeToken.PR_VOID);

    // Tipo -> TipoBase DimensionesOpcionales
    public static final ConjuntoDePrimeros TIPO_TIPO_BASE = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT);

    // TipoBase -> TipoPrimitivo
    public static final ConjuntoDePrimeros TIPO_BASE_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // TipoBase -> TipoReferencia
    public static final ConjuntoDePrimeros TIPO_BASE_TIPO_REFERENCIA = de(TipoDeToken.ID_CLASE);

    // TipoBase -> idGen
    public static final ConjuntoDePrimeros TIPO_BASE_ID_GEN = de(TipoDeToken.ID_GEN);

    // DimensionesOpcionales -> [ ] DimensionesOpcionales
    public static final ConjuntoDePrimeros DIMENSIONES_OPCIONALES_CORCHETE_A = de(TipoDeToken.CORCHETE_A);

    // DimensionesOpcionales -> EPSILON
    public static final ConjuntoDePrimeros DIMENSIONES_OPCIONALES = de();

    // TipoReferencia -> idClase TipoGenericoOpcional
    public static final ConjuntoDePrimeros TIPO_REFERENCIA_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // TipoPrimitivo -> boolean
    public static final ConjuntoDePrimeros TIPO_PRIMITIVO_BOOLEAN = de(TipoDeToken.PR_BOOLEAN);

    // TipoPrimitivo -> char
    public static final ConjuntoDePrimeros TIPO_PRIMITIVO_CHAR = de(TipoDeToken.PR_CHAR);

    // TipoPrimitivo -> int
    public static final ConjuntoDePrimeros TIPO_PRIMITIVO_INT = de(TipoDeToken.PR_INT);

    // TipoPrimitivo -> float
    public static final ConjuntoDePrimeros TIPO_PRIMITIVO_FLOAT = de(TipoDeToken.PR_FLOAT);

    // TipoGenericoOpcional -> < ArgumentoDeTipo >
    public static final ConjuntoDePrimeros TIPO_GENERICO_OPCIONAL_MENOR = de(TipoDeToken.OP_MENOR);

    // TipoGenericoOpcional -> EPSILON
    public static final ConjuntoDePrimeros TIPO_GENERICO_OPCIONAL = de();

    // ArgumentoDeTipo -> idGen
    public static final ConjuntoDePrimeros ARGUMENTO_DE_TIPO_ID_GEN = de(TipoDeToken.ID_GEN);

    // ArgumentoDeTipo -> idClase TipoGenericoOpcional
    public static final ConjuntoDePrimeros ARGUMENTO_DE_TIPO_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // ArgumentoDeTipo -> idClase
    public static final ConjuntoDePrimeros ARGUMENTO_DE_TIPO_ID_CLASE_2 = de(TipoDeToken.ID_CLASE);

    // ArgsFormales -> ( ListaArgsFormalesOpcional )
    public static final ConjuntoDePrimeros ARGS_FORMALES_PAREN_A = de(TipoDeToken.PAREN_A);

    // ListaArgsFormalesOpcional -> ListaArgsFormales
    public static final ConjuntoDePrimeros LISTA_ARGS_FORMALES_OPCIONAL_LISTA_ARGS_FORMALES = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT);

    // ListaArgsFormalesOpcional -> EPSILON
    public static final ConjuntoDePrimeros LISTA_ARGS_FORMALES_OPCIONAL = de();

    // ListaArgsFormales -> ArgFormal RestoListaArgsFormales
    public static final ConjuntoDePrimeros LISTA_ARGS_FORMALES_ARG_FORMAL = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT);

    // RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales
    public static final ConjuntoDePrimeros RESTO_LISTA_ARGS_FORMALES_COMA = de(TipoDeToken.COMA);

    // RestoListaArgsFormales -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_ARGS_FORMALES = de();

    // ArgFormal -> Tipo idMetVar
    public static final ConjuntoDePrimeros ARG_FORMAL_TIPO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT);

    // Bloque -> { ListaSentencias }
    public static final ConjuntoDePrimeros BLOQUE_LLAVE_A = de(TipoDeToken.LLAVE_A);

    // ListaSentencias -> Sentencia ListaSentencias
    public static final ConjuntoDePrimeros LISTA_SENTENCIAS_SENTENCIA = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.PUNTO_Y_COMA, TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.CHAR_LITERAL,
            TipoDeToken.PR_FALSE, TipoDeToken.PR_FLOAT, TipoDeToken.FLOAT_LITERAL, TipoDeToken.PR_FOR,
            TipoDeToken.ID_CLASE, TipoDeToken.ID_GEN, TipoDeToken.ID_MV, TipoDeToken.PR_IF,
            TipoDeToken.PR_INT, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.PR_RETURN, TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE,
            TipoDeToken.PR_VAR, TipoDeToken.PR_WHILE, TipoDeToken.LLAVE_A);

    // ListaSentencias -> EPSILON
    public static final ConjuntoDePrimeros LISTA_SENTENCIAS = de();

    // Sentencia -> ;
    public static final ConjuntoDePrimeros SENTENCIA_PUNTO_Y_COMA = de(TipoDeToken.PUNTO_Y_COMA);

    // Sentencia -> VarLocal ;
    public static final ConjuntoDePrimeros SENTENCIA_VAR_LOCAL = de(TipoDeToken.PR_VAR);

    // Sentencia -> Return ;
    public static final ConjuntoDePrimeros SENTENCIA_RETURN = de(TipoDeToken.PR_RETURN);

    // Sentencia -> If
    public static final ConjuntoDePrimeros SENTENCIA_IF = de(TipoDeToken.PR_IF);

    // Sentencia -> While
    public static final ConjuntoDePrimeros SENTENCIA_WHILE = de(TipoDeToken.PR_WHILE);

    // Sentencia -> Bloque
    public static final ConjuntoDePrimeros SENTENCIA_BLOQUE = de(TipoDeToken.LLAVE_A);

    // Sentencia -> For
    public static final ConjuntoDePrimeros SENTENCIA_FOR = de(TipoDeToken.PR_FOR);

    // Sentencia -> SentenciaExpresion ;
    public static final ConjuntoDePrimeros SENTENCIA_SENTENCIA_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // SentenciaExpresion -> Expresion
    public static final ConjuntoDePrimeros SENTENCIA_EXPRESION_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // Sentencia -> ExpresionSinIdClase ;
    public static final ConjuntoDePrimeros SENTENCIA_EXPRESION_SIN_ID_CLASE = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_MV,
            TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL, TipoDeToken.LIT_STRING,
            TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // Sentencia -> TipoPrimitivo DimensionesOpcionales ListaDeclaradores ;
    public static final ConjuntoDePrimeros SENTENCIA_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // Sentencia -> idGen DimensionesOpcionales ListaDeclaradores ;
    public static final ConjuntoDePrimeros SENTENCIA_ID_GEN = de(TipoDeToken.ID_GEN);

    // Sentencia -> idClase SentenciaDesdeIdClase
    public static final ConjuntoDePrimeros SENTENCIA_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // SentenciaDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales ListaDeclaradores ;
    public static final ConjuntoDePrimeros SENTENCIA_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL = de(
            TipoDeToken.OP_MENOR, TipoDeToken.CORCHETE_A, TipoDeToken.ID_MV);

    // SentenciaDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ;
    public static final ConjuntoDePrimeros SENTENCIA_DESDE_ID_CLASE_PUNTO = de(TipoDeToken.PUNTO);

    // VarLocal -> var idMetVar = ExpresionCondicional
    public static final ConjuntoDePrimeros VAR_LOCAL_VAR = de(TipoDeToken.PR_VAR);

    // ListaDeclaradores -> idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    public static final ConjuntoDePrimeros LISTA_DECLARADORES_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // RestoListaDeclaradores -> , idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    public static final ConjuntoDePrimeros RESTO_LISTA_DECLARADORES_COMA = de(TipoDeToken.COMA);

    // RestoListaDeclaradores -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_DECLARADORES = de();

    // InicializacionDeDeclarador -> = ExpresionCondicional
    public static final ConjuntoDePrimeros INICIALIZACION_DE_DECLARADOR_ASIGNACION = de(
            TipoDeToken.OP_ASIGNACION);

    // InicializacionDeDeclarador -> EPSILON
    public static final ConjuntoDePrimeros INICIALIZACION_DE_DECLARADOR = de();

    // Return -> return ExpresionOpcional
    public static final ConjuntoDePrimeros RETURN_RETURN = de(TipoDeToken.PR_RETURN);

    // ExpresionOpcional -> Expresion
    public static final ConjuntoDePrimeros EXPRESION_OPCIONAL_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // ExpresionOpcional -> EPSILON
    public static final ConjuntoDePrimeros EXPRESION_OPCIONAL = de();

    // If -> if ( Expresion ) Sentencia ElseOpcional
    public static final ConjuntoDePrimeros IF_IF = de(TipoDeToken.PR_IF);

    // ElseOpcional -> else Sentencia
    public static final ConjuntoDePrimeros ELSE_OPCIONAL_ELSE = de(TipoDeToken.PR_ELSE);

    // ElseOpcional -> EPSILON
    public static final ConjuntoDePrimeros ELSE_OPCIONAL = de();

    // While -> while ( Expresion ) Sentencia
    public static final ConjuntoDePrimeros WHILE_WHILE = de(TipoDeToken.PR_WHILE);

    // For -> for ( EncabezadoFor ) Sentencia
    public static final ConjuntoDePrimeros FOR_FOR = de(TipoDeToken.PR_FOR);

    // EncabezadoFor -> ; ExpresionOpcional ; ExpresionOpcional
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_PUNTO_Y_COMA = de(TipoDeToken.PUNTO_Y_COMA);

    // EncabezadoFor -> var idMetVar EncabezadoForTrasVar
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_VAR = de(TipoDeToken.PR_VAR);

    // EncabezadoFor -> TipoPrimitivo DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // EncabezadoFor -> idGen DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_ID_GEN = de(TipoDeToken.ID_GEN);

    // EncabezadoFor -> idClase EncabezadoForDesdeIdClase
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // EncabezadoFor -> ExpresionSinIdClase ClausulasFinalesFor
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_EXPRESION_SIN_ID_CLASE = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_MV,
            TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL, TipoDeToken.LIT_STRING,
            TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // EncabezadoForDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL = de(
            TipoDeToken.OP_MENOR, TipoDeToken.CORCHETE_A, TipoDeToken.ID_MV);

    // EncabezadoForDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ClausulasFinalesFor
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_DESDE_ID_CLASE_PUNTO = de(TipoDeToken.PUNTO);

    // EncabezadoForTrasVar -> : Expresion
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS = de(TipoDeToken.DOS_PUNTOS);

    // EncabezadoForTrasVar -> = ExpresionCondicional ClausulasFinalesFor
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TRAS_VAR_ASIGNACION = de(TipoDeToken.OP_ASIGNACION);

    // EncabezadoForTrasDeclarador -> : Expresion
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TRAS_DECLARADOR_DOS_PUNTOS = de(
            TipoDeToken.DOS_PUNTOS);

    // EncabezadoForTrasDeclarador -> InicializacionDeDeclaradorFor ClausulasFinalesFor
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TRAS_DECLARADOR_INICIALIZACION_DE_DECLARADOR_FOR = de(
            TipoDeToken.PUNTO_Y_COMA, TipoDeToken.OP_ASIGNACION);

    // InicializacionDeDeclaradorFor -> = ExpresionCondicional
    public static final ConjuntoDePrimeros INICIALIZACION_DE_DECLARADOR_FOR_ASIGNACION = de(
            TipoDeToken.OP_ASIGNACION);

    // InicializacionDeDeclaradorFor -> EPSILON
    public static final ConjuntoDePrimeros INICIALIZACION_DE_DECLARADOR_FOR = de();

    // ClausulasFinalesFor -> ; ExpresionOpcional ; ExpresionOpcional
    public static final ConjuntoDePrimeros CLAUSULAS_FINALES_FOR_PUNTO_Y_COMA = de(TipoDeToken.PUNTO_Y_COMA);

    // Expresion -> ExpresionCondicional RestoExpresion
    public static final ConjuntoDePrimeros EXPRESION_EXPRESION_CONDICIONAL = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoExpresion -> OperadorAsignacion ExpresionCondicional
    public static final ConjuntoDePrimeros RESTO_EXPRESION_OPERADOR_ASIGNACION = de(
            TipoDeToken.OP_ASIGNACION);

    // RestoExpresion -> EPSILON
    public static final ConjuntoDePrimeros RESTO_EXPRESION = de();

    // OperadorAsignacion -> =
    public static final ConjuntoDePrimeros OPERADOR_ASIGNACION_ASIGNACION = de(TipoDeToken.OP_ASIGNACION);

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    public static final ConjuntoDePrimeros EXPRESION_CONDICIONAL_EXPRESION_COMPUESTA = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoCondicional -> ? Expresion : ExpresionCondicional
    public static final ConjuntoDePrimeros RESTO_CONDICIONAL_SIGNO_PREGUNTA = de(TipoDeToken.SIGNO_PREGUNTA);

    // RestoCondicional -> EPSILON
    public static final ConjuntoDePrimeros RESTO_CONDICIONAL = de();

    // ExpresionCompuesta -> ExpresionBasica RestoExpresionCompuesta
    public static final ConjuntoDePrimeros EXPRESION_COMPUESTA_EXPRESION_BASICA = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta
    public static final ConjuntoDePrimeros RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR,
            TipoDeToken.OP_MENOR_IGUAL, TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR,
            TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.OP_OR);

    // RestoExpresionCompuesta -> EPSILON
    public static final ConjuntoDePrimeros RESTO_EXPRESION_COMPUESTA = de();

    // OperadorBinario -> ||
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_OR = de(TipoDeToken.OP_OR);

    // OperadorBinario -> &&
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_AND = de(TipoDeToken.OP_AND);

    // OperadorBinario -> ==
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_IGUAL = de(TipoDeToken.OP_IGUAL);

    // OperadorBinario -> !=
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_DISTINTO = de(TipoDeToken.OP_DISTINTO);

    // OperadorBinario -> <
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_MENOR = de(TipoDeToken.OP_MENOR);

    // OperadorBinario -> >
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_MAYOR = de(TipoDeToken.OP_MAYOR);

    // OperadorBinario -> <=
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_MENOR_IGUAL = de(TipoDeToken.OP_MENOR_IGUAL);

    // OperadorBinario -> >=
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_MAYOR_IGUAL = de(TipoDeToken.OP_MAYOR_IGUAL);

    // OperadorBinario -> +
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_SUMA = de(TipoDeToken.OP_SUMA);

    // OperadorBinario -> -
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_RESTA = de(TipoDeToken.OP_RESTA);

    // OperadorBinario -> *
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_MULT = de(TipoDeToken.OP_MULT);

    // OperadorBinario -> /
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_DIV = de(TipoDeToken.OP_DIV);

    // OperadorBinario -> %
    public static final ConjuntoDePrimeros OPERADOR_BINARIO_MODULO = de(TipoDeToken.OP_MODULO);

    // ExpresionBasica -> OperadorUnario Operando
    public static final ConjuntoDePrimeros EXPRESION_BASICA_OPERADOR_UNARIO = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA);

    // ExpresionBasica -> Operando
    public static final ConjuntoDePrimeros EXPRESION_BASICA_OPERANDO = de(
            TipoDeToken.PAREN_A, TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL,
            TipoDeToken.ID_CLASE, TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW,
            TipoDeToken.PR_NULL, TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // OperadorUnario -> +
    public static final ConjuntoDePrimeros OPERADOR_UNARIO_SUMA = de(TipoDeToken.OP_SUMA);

    // OperadorUnario -> -
    public static final ConjuntoDePrimeros OPERADOR_UNARIO_RESTA = de(TipoDeToken.OP_RESTA);

    // OperadorUnario -> !
    public static final ConjuntoDePrimeros OPERADOR_UNARIO_NEGACION = de(TipoDeToken.OP_NEGACION);

    // Operando -> Primitivo
    public static final ConjuntoDePrimeros OPERANDO_PRIMITIVO = de(
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL,
            TipoDeToken.INT_LITERAL, TipoDeToken.PR_NULL, TipoDeToken.PR_TRUE);

    // Operando -> Referencia PosfijoOpcional
    public static final ConjuntoDePrimeros OPERANDO_REFERENCIA = de(
            TipoDeToken.PAREN_A, TipoDeToken.ID_CLASE, TipoDeToken.ID_MV, TipoDeToken.PR_NEW,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS);

    // PosfijoOpcional -> ++
    public static final ConjuntoDePrimeros POSFIJO_OPCIONAL_INCREMENTO = de(TipoDeToken.OP_INCREMENTO);

    // PosfijoOpcional -> --
    public static final ConjuntoDePrimeros POSFIJO_OPCIONAL_DECREMENTO = de(TipoDeToken.OP_DECREMENTO);

    // PosfijoOpcional -> EPSILON
    public static final ConjuntoDePrimeros POSFIJO_OPCIONAL = de();

    // Primitivo -> true
    public static final ConjuntoDePrimeros PRIMITIVO_TRUE = de(TipoDeToken.PR_TRUE);

    // Primitivo -> false
    public static final ConjuntoDePrimeros PRIMITIVO_FALSE = de(TipoDeToken.PR_FALSE);

    // Primitivo -> intLiteral
    public static final ConjuntoDePrimeros PRIMITIVO_INT_LITERAL = de(TipoDeToken.INT_LITERAL);

    // Primitivo -> charLiteral
    public static final ConjuntoDePrimeros PRIMITIVO_CHAR_LITERAL = de(TipoDeToken.CHAR_LITERAL);

    // Primitivo -> null
    public static final ConjuntoDePrimeros PRIMITIVO_NULL = de(TipoDeToken.PR_NULL);

    // Primitivo -> floatLiteral
    public static final ConjuntoDePrimeros PRIMITIVO_FLOAT_LITERAL = de(TipoDeToken.FLOAT_LITERAL);

    // ExpresionSinIdClase -> OperadorUnario Operando ColaTrasOperando
    public static final ConjuntoDePrimeros EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA);

    // ExpresionSinIdClase -> Primitivo ColaTrasOperando
    public static final ConjuntoDePrimeros EXPRESION_SIN_ID_CLASE_PRIMITIVO = de(
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL,
            TipoDeToken.INT_LITERAL, TipoDeToken.PR_NULL, TipoDeToken.PR_TRUE);

    // ExpresionSinIdClase -> PrimarioSinIdClase ColaDeExpresionDesdePrimario
    public static final ConjuntoDePrimeros EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE = de(
            TipoDeToken.PAREN_A, TipoDeToken.ID_MV, TipoDeToken.PR_NEW, TipoDeToken.LIT_STRING,
            TipoDeToken.PR_THIS);

    // ColaTrasOperando -> RestoExpresionCompuesta RestoCondicional RestoExpresion
    public static final ConjuntoDePrimeros COLA_TRAS_OPERANDO_RESTO_EXPRESION_COMPUESTA = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR,
            TipoDeToken.OP_MENOR_IGUAL, TipoDeToken.OP_ASIGNACION, TipoDeToken.OP_IGUAL,
            TipoDeToken.OP_MAYOR, TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.OP_OR);

    // ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
    public static final ConjuntoDePrimeros COLA_DE_EXPRESION_DESDE_PRIMARIO_RESTO_REFERENCIA = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_INCREMENTO, TipoDeToken.OP_RESTA, TipoDeToken.OP_DECREMENTO,
            TipoDeToken.PUNTO, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR, TipoDeToken.OP_MENOR_IGUAL,
            TipoDeToken.OP_ASIGNACION, TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR,
            TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.CORCHETE_A,
            TipoDeToken.OP_OR);

    // Referencia -> Primario RestoReferencia
    public static final ConjuntoDePrimeros REFERENCIA_PRIMARIO = de(
            TipoDeToken.PAREN_A, TipoDeToken.ID_CLASE, TipoDeToken.ID_MV, TipoDeToken.PR_NEW,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS);

    // RestoReferencia -> Encadenado RestoReferencia
    public static final ConjuntoDePrimeros RESTO_REFERENCIA_ENCADENADO = de(TipoDeToken.PUNTO);

    // RestoReferencia -> AccesoArreglo RestoReferencia
    public static final ConjuntoDePrimeros RESTO_REFERENCIA_ACCESO_ARREGLO = de(TipoDeToken.CORCHETE_A);

    // RestoReferencia -> EPSILON
    public static final ConjuntoDePrimeros RESTO_REFERENCIA = de();

    // Encadenado -> . idMetVar EncadenadoAtributoOMetodo
    public static final ConjuntoDePrimeros ENCADENADO_PUNTO = de(TipoDeToken.PUNTO);

    // EncadenadoAtributoOMetodo -> ArgsActuales
    public static final ConjuntoDePrimeros ENCADENADO_ATRIBUTO_OMETODO_ARGS_ACTUALES = de(
            TipoDeToken.PAREN_A);

    // EncadenadoAtributoOMetodo -> EPSILON
    public static final ConjuntoDePrimeros ENCADENADO_ATRIBUTO_OMETODO = de();

    // AccesoArreglo -> [ Expresion ]
    public static final ConjuntoDePrimeros ACCESO_ARREGLO_CORCHETE_A = de(TipoDeToken.CORCHETE_A);

    // Primario -> LlamadaMetodoEstatico
    public static final ConjuntoDePrimeros PRIMARIO_LLAMADA_METODO_ESTATICO = de(TipoDeToken.ID_CLASE);

    // Primario -> PrimarioSinIdClase
    public static final ConjuntoDePrimeros PRIMARIO_PRIMARIO_SIN_ID_CLASE = de(
            TipoDeToken.PAREN_A, TipoDeToken.ID_MV, TipoDeToken.PR_NEW, TipoDeToken.LIT_STRING,
            TipoDeToken.PR_THIS);

    // LlamadaMetodoEstatico -> idClase . idMetVar ArgsActuales
    public static final ConjuntoDePrimeros LLAMADA_METODO_ESTATICO_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // PrimarioSinIdClase -> this
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_THIS = de(TipoDeToken.PR_THIS);

    // PrimarioSinIdClase -> stringLiteral
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_STRING_LITERAL = de(TipoDeToken.LIT_STRING);

    // PrimarioSinIdClase -> ParentizadaOLambda
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_PARENTIZADA_OLAMBDA = de(
            TipoDeToken.PAREN_A);

    // PrimarioSinIdClase -> AccesoVarLlamadaOLambda
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_LLAMADA_OLAMBDA = de(
            TipoDeToken.ID_MV);

    // PrimarioSinIdClase -> Creacion
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_CREACION = de(TipoDeToken.PR_NEW);

    // AccesoVarLlamadaOLambda -> idMetVar AccesoLlamadaOLambda
    public static final ConjuntoDePrimeros ACCESO_VAR_LLAMADA_OLAMBDA_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // AccesoLlamadaOLambda -> ArgsActuales
    public static final ConjuntoDePrimeros ACCESO_LLAMADA_OLAMBDA_ARGS_ACTUALES = de(TipoDeToken.PAREN_A);

    // AccesoLlamadaOLambda -> -> CuerpoLambda
    public static final ConjuntoDePrimeros ACCESO_LLAMADA_OLAMBDA_FLECHA = de(TipoDeToken.OP_FLECHA);

    // AccesoLlamadaOLambda -> EPSILON
    public static final ConjuntoDePrimeros ACCESO_LLAMADA_OLAMBDA = de();

    // ParentizadaOLambda -> ( ContenidoDeParentesis
    public static final ConjuntoDePrimeros PARENTIZADA_OLAMBDA_PAREN_A = de(TipoDeToken.PAREN_A);

    // ContenidoDeParentesis -> ) -> CuerpoLambda
    public static final ConjuntoDePrimeros CONTENIDO_DE_PARENTESIS_PAREN_C = de(TipoDeToken.PAREN_C);

    // ContenidoDeParentesis -> Expresion RestoDeParentesis
    public static final ConjuntoDePrimeros CONTENIDO_DE_PARENTESIS_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoDeParentesis -> ) FinDeParentizadaOLambda
    public static final ConjuntoDePrimeros RESTO_DE_PARENTESIS_PAREN_C = de(TipoDeToken.PAREN_C);

    // RestoDeParentesis -> , ListaParametrosLambda ) -> CuerpoLambda
    public static final ConjuntoDePrimeros RESTO_DE_PARENTESIS_COMA = de(TipoDeToken.COMA);

    // FinDeParentizadaOLambda -> -> CuerpoLambda
    public static final ConjuntoDePrimeros FIN_DE_PARENTIZADA_OLAMBDA_FLECHA = de(TipoDeToken.OP_FLECHA);

    // FinDeParentizadaOLambda -> EPSILON
    public static final ConjuntoDePrimeros FIN_DE_PARENTIZADA_OLAMBDA = de();

    // ListaParametrosLambda -> idMetVar RestoListaParametrosLambda
    public static final ConjuntoDePrimeros LISTA_PARAMETROS_LAMBDA_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // RestoListaParametrosLambda -> , idMetVar RestoListaParametrosLambda
    public static final ConjuntoDePrimeros RESTO_LISTA_PARAMETROS_LAMBDA_COMA = de(TipoDeToken.COMA);

    // RestoListaParametrosLambda -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_PARAMETROS_LAMBDA = de();

    // CuerpoLambda -> Expresion
    public static final ConjuntoDePrimeros CUERPO_LAMBDA_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // Creacion -> new ArregloOInstancia
    public static final ConjuntoDePrimeros CREACION_NEW = de(TipoDeToken.PR_NEW);

    // ArregloOInstancia -> TipoPrimitivo CreacionDeArreglo
    public static final ConjuntoDePrimeros ARREGLO_OINSTANCIA_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // ArregloOInstancia -> idGen CreacionDeArreglo
    public static final ConjuntoDePrimeros ARREGLO_OINSTANCIA_ID_GEN = de(TipoDeToken.ID_GEN);

    // ArregloOInstancia -> idClase CreacionDesdeIdClase
    public static final ConjuntoDePrimeros ARREGLO_OINSTANCIA_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // CreacionDesdeIdClase -> < GenericoDeCreacion
    public static final ConjuntoDePrimeros CREACION_DESDE_ID_CLASE_MENOR = de(TipoDeToken.OP_MENOR);

    // CreacionDesdeIdClase -> CreacionDeArreglo
    public static final ConjuntoDePrimeros CREACION_DESDE_ID_CLASE_CREACION_DE_ARREGLO = de(
            TipoDeToken.CORCHETE_A);

    // CreacionDesdeIdClase -> ArgsActuales
    public static final ConjuntoDePrimeros CREACION_DESDE_ID_CLASE_ARGS_ACTUALES = de(TipoDeToken.PAREN_A);

    // GenericoDeCreacion -> > ArgsActuales
    public static final ConjuntoDePrimeros GENERICO_DE_CREACION_MAYOR = de(TipoDeToken.OP_MAYOR);

    // GenericoDeCreacion -> ArgumentoDeTipo > ArregloOInstanciaDeClase
    public static final ConjuntoDePrimeros GENERICO_DE_CREACION_ARGUMENTO_DE_TIPO = de(
            TipoDeToken.ID_CLASE, TipoDeToken.ID_GEN);

    // ArregloOInstanciaDeClase -> CreacionDeArreglo
    public static final ConjuntoDePrimeros ARREGLO_OINSTANCIA_DE_CLASE_CREACION_DE_ARREGLO = de(
            TipoDeToken.CORCHETE_A);

    // ArregloOInstanciaDeClase -> ArgsActuales
    public static final ConjuntoDePrimeros ARREGLO_OINSTANCIA_DE_CLASE_ARGS_ACTUALES = de(
            TipoDeToken.PAREN_A);

    // CreacionDeArreglo -> [ RestoCreacionDeArreglo
    public static final ConjuntoDePrimeros CREACION_DE_ARREGLO_CORCHETE_A = de(TipoDeToken.CORCHETE_A);

    // RestoCreacionDeArreglo -> Expresion ] DimensionesConTamanioOpcional
    public static final ConjuntoDePrimeros RESTO_CREACION_DE_ARREGLO_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoCreacionDeArreglo -> ] DimensionesVaciasOpcional InicializadorDeArreglo
    public static final ConjuntoDePrimeros RESTO_CREACION_DE_ARREGLO_CORCHETE_C = de(TipoDeToken.CORCHETE_C);

    // DimensionesVaciasOpcional -> [ ] DimensionesVaciasOpcional
    public static final ConjuntoDePrimeros DIMENSIONES_VACIAS_OPCIONAL_CORCHETE_A = de(
            TipoDeToken.CORCHETE_A);

    // DimensionesVaciasOpcional -> EPSILON
    public static final ConjuntoDePrimeros DIMENSIONES_VACIAS_OPCIONAL = de();

    // InicializadorDeArreglo -> { ListaElementosOpcional }
    public static final ConjuntoDePrimeros INICIALIZADOR_DE_ARREGLO_LLAVE_A = de(TipoDeToken.LLAVE_A);

    // ListaElementosOpcional -> ListaElementos
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_OPCIONAL_LISTA_ELEMENTOS = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE, TipoDeToken.LLAVE_A);

    // ListaElementosOpcional -> EPSILON
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_OPCIONAL = de();

    // ListaElementos -> ElementoInicializador RestoListaElementos
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_ELEMENTO_INICIALIZADOR = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE, TipoDeToken.LLAVE_A);

    // RestoListaElementos -> , ElementoInicializador RestoListaElementos
    public static final ConjuntoDePrimeros RESTO_LISTA_ELEMENTOS_COMA = de(TipoDeToken.COMA);

    // RestoListaElementos -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_ELEMENTOS = de();

    // ElementoInicializador -> Expresion
    public static final ConjuntoDePrimeros ELEMENTO_INICIALIZADOR_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // ElementoInicializador -> InicializadorDeArreglo
    public static final ConjuntoDePrimeros ELEMENTO_INICIALIZADOR_INICIALIZADOR_DE_ARREGLO = de(
            TipoDeToken.LLAVE_A);

    // DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
    public static final ConjuntoDePrimeros DIMENSIONES_CON_TAMANIO_CORCHETE_A = de(TipoDeToken.CORCHETE_A);

    // DimensionesConTamanioOpcional -> DimensionesConTamanio
    public static final ConjuntoDePrimeros DIMENSIONES_CON_TAMANIO_OPCIONAL_DIMENSIONES_CON_TAMANIO = de(
            TipoDeToken.CORCHETE_A);

    // DimensionesConTamanioOpcional -> EPSILON
    public static final ConjuntoDePrimeros DIMENSIONES_CON_TAMANIO_OPCIONAL = de();

    // ArgsActuales -> ( ListaExpsOpcional )
    public static final ConjuntoDePrimeros ARGS_ACTUALES_PAREN_A = de(TipoDeToken.PAREN_A);

    // ListaExpsOpcional -> ListaExps
    public static final ConjuntoDePrimeros LISTA_EXPS_OPCIONAL_LISTA_EXPS = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // ListaExpsOpcional -> EPSILON
    public static final ConjuntoDePrimeros LISTA_EXPS_OPCIONAL = de();

    // ListaExps -> Expresion RestoListaExps
    public static final ConjuntoDePrimeros LISTA_EXPS_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoListaExps -> , Expresion RestoListaExps
    public static final ConjuntoDePrimeros RESTO_LISTA_EXPS_COMA = de(TipoDeToken.COMA);

    // RestoListaExps -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_EXPS = de();

    private Primeros() {
    }
}
