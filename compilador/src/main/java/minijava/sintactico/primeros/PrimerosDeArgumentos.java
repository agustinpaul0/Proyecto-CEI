package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// La lista de argumentos actuales de una llamada.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeArgumentos {

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

    // RestoListaExps -> , ListaExps
    public static final ConjuntoDePrimeros RESTO_LISTA_EXPS_COMA = de(TipoDeToken.COMA);

    // RestoListaExps -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_EXPS = de();

    private PrimerosDeArgumentos() {
    }
}
