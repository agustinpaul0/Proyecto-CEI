package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// Los bloques y las sentencias.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeSentencias {

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

    // VarLocal -> var idMetVar = ExpresionCondicionalOLambda
    public static final ConjuntoDePrimeros VAR_LOCAL_VAR = de(TipoDeToken.PR_VAR);

    // ListaDeclaradores -> idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    public static final ConjuntoDePrimeros LISTA_DECLARADORES_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // RestoListaDeclaradores -> , idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    public static final ConjuntoDePrimeros RESTO_LISTA_DECLARADORES_COMA = de(TipoDeToken.COMA);

    // RestoListaDeclaradores -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_DECLARADORES = de();

    // InicializacionDeDeclarador -> = ExpresionCondicionalOLambda
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

    private PrimerosDeSentencias() {
    }
}
