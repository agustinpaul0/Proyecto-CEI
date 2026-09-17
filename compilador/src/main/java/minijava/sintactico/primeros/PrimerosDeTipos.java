package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// Los tipos, las dimensiones y los argumentos formales.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeTipos {

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

    private PrimerosDeTipos() {
    }
}
