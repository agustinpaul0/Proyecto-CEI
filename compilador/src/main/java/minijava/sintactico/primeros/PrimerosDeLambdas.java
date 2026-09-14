package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-logros.g. No editar a mano: se regenera.
//
// El logro MiniLambdas E2 y la desambiguacion del parentesis.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
// Las producciones que existen por un logro se emiten igual: el analizador
// consulta el logro por su cuenta antes de tomar la rama.
public final class PrimerosDeLambdas {

    // AccesoLlamadaOLambda -> -> CuerpoLambda
    public static final ConjuntoDePrimeros ACCESO_LLAMADA_OLAMBDA_FLECHA = de(TipoDeToken.OP_FLECHA);

    // AccesoLlamadaOLambda -> AccesoLlamada ColaDesdePrimarioSinAsignacion
    public static final ConjuntoDePrimeros ACCESO_LLAMADA_OLAMBDA_ACCESO_LLAMADA = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.PAREN_A,
            TipoDeToken.OP_MULT, TipoDeToken.OP_SUMA, TipoDeToken.OP_INCREMENTO, TipoDeToken.OP_RESTA,
            TipoDeToken.OP_DECREMENTO, TipoDeToken.PUNTO, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR,
            TipoDeToken.OP_MENOR_IGUAL, TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR,
            TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.CORCHETE_A,
            TipoDeToken.OP_OR);

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

    // FinDeParentizadaOLambda -> ColaDesdePrimarioSinAsignacion
    public static final ConjuntoDePrimeros FIN_DE_PARENTIZADA_OLAMBDA_COLA_DESDE_PRIMARIO_SIN_ASIGNACION = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_INCREMENTO, TipoDeToken.OP_RESTA, TipoDeToken.OP_DECREMENTO,
            TipoDeToken.PUNTO, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR, TipoDeToken.OP_MENOR_IGUAL,
            TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR, TipoDeToken.OP_MAYOR_IGUAL,
            TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.CORCHETE_A, TipoDeToken.OP_OR);

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

    private PrimerosDeLambdas() {
    }
}
