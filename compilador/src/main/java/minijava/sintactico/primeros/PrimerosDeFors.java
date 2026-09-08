package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-logros.g. No editar a mano: se regenera.
//
// El logro Fors! E2.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
// Las producciones que existen por un logro se emiten igual: el analizador
// consulta el logro por su cuenta antes de tomar la rama.
public final class PrimerosDeFors {

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

    private PrimerosDeFors() {
    }
}
