package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// La sentencia for, estandar y de iteradores.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeFors {

    // For -> for ( EncabezadoFor ) Sentencia
    public static final ConjuntoDePrimeros FOR_FOR = de(TipoDeToken.PR_FOR);

    // EncabezadoFor -> ; ExpresionOpcional ; ExpresionOpcional
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_PUNTO_Y_COMA = de(TipoDeToken.PUNTO_Y_COMA);

    // EncabezadoFor -> var idMetVar EncabezadoForTrasVar
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_VAR = de(TipoDeToken.PR_VAR);

    // EncabezadoFor -> Expresion ClausulasFinalesFor
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // EncabezadoForTrasVar -> : Expresion
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS = de(TipoDeToken.DOS_PUNTOS);

    // EncabezadoForTrasVar -> = ExpresionCondicional ClausulasFinalesFor
    public static final ConjuntoDePrimeros ENCABEZADO_FOR_TRAS_VAR_ASIGNACION = de(TipoDeToken.OP_ASIGNACION);

    // ClausulasFinalesFor -> ; ExpresionOpcional ; ExpresionOpcional
    public static final ConjuntoDePrimeros CLAUSULAS_FINALES_FOR_PUNTO_Y_COMA = de(TipoDeToken.PUNTO_Y_COMA);

    private PrimerosDeFors() {
    }
}
