package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-logros.g. No editar a mano: se regenera.
//
// Las capas de precedencia de las expresiones.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
// Las producciones que existen por un logro se emiten igual: el analizador
// consulta el logro por su cuenta antes de tomar la rama.
public final class PrimerosDeExpresiones {

    // Expresion -> ExpresionCondicionalOLambda RestoExpresion
    public static final ConjuntoDePrimeros EXPRESION_EXPRESION_CONDICIONAL_OLAMBDA = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // ExpresionSinLambda -> ExpresionCondicional RestoExpresion
    public static final ConjuntoDePrimeros EXPRESION_SIN_LAMBDA_EXPRESION_CONDICIONAL = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoExpresion -> OperadorAsignacion ExpresionCondicionalOLambda
    public static final ConjuntoDePrimeros RESTO_EXPRESION_OPERADOR_ASIGNACION = de(
            TipoDeToken.OP_ASIGNACION);

    // RestoExpresion -> EPSILON
    public static final ConjuntoDePrimeros RESTO_EXPRESION = de();

    // OperadorAsignacion -> =
    public static final ConjuntoDePrimeros OPERADOR_ASIGNACION_ASIGNACION = de(TipoDeToken.OP_ASIGNACION);

    // ExpresionCondicionalOLambda -> OperadorUnario Operando ColaTrasOperandoSinAsignacion
    public static final ConjuntoDePrimeros EXPRESION_CONDICIONAL_OLAMBDA_OPERADOR_UNARIO = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA);

    // ExpresionCondicionalOLambda -> Primitivo ColaTrasOperandoSinAsignacion
    public static final ConjuntoDePrimeros EXPRESION_CONDICIONAL_OLAMBDA_PRIMITIVO = de(
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL,
            TipoDeToken.INT_LITERAL, TipoDeToken.PR_NULL, TipoDeToken.PR_TRUE);

    // ExpresionCondicionalOLambda -> ExpresionDesdePrimarioOLambda
    public static final ConjuntoDePrimeros EXPRESION_CONDICIONAL_OLAMBDA_EXPRESION_DESDE_PRIMARIO_OLAMBDA = de(
            TipoDeToken.PAREN_A, TipoDeToken.ID_CLASE, TipoDeToken.ID_MV, TipoDeToken.PR_NEW,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS);

    // ExpresionDesdePrimarioOLambda -> idClase . idMetVar ArgsActuales ColaDesdePrimarioSinAsignacion
    public static final ConjuntoDePrimeros EXPRESION_DESDE_PRIMARIO_OLAMBDA_ID_CLASE = de(
            TipoDeToken.ID_CLASE);

    // ExpresionDesdePrimarioOLambda -> this ColaDesdePrimarioSinAsignacion
    public static final ConjuntoDePrimeros EXPRESION_DESDE_PRIMARIO_OLAMBDA_THIS = de(TipoDeToken.PR_THIS);

    // ExpresionDesdePrimarioOLambda -> stringLiteral ColaDesdePrimarioSinAsignacion
    public static final ConjuntoDePrimeros EXPRESION_DESDE_PRIMARIO_OLAMBDA_STRING_LITERAL = de(
            TipoDeToken.LIT_STRING);

    // ExpresionDesdePrimarioOLambda -> Creacion ColaDesdePrimarioSinAsignacion
    public static final ConjuntoDePrimeros EXPRESION_DESDE_PRIMARIO_OLAMBDA_CREACION = de(TipoDeToken.PR_NEW);

    // ExpresionDesdePrimarioOLambda -> idMetVar AccesoLlamadaOLambda
    public static final ConjuntoDePrimeros EXPRESION_DESDE_PRIMARIO_OLAMBDA_ID_MET_VAR = de(
            TipoDeToken.ID_MV);

    // ExpresionDesdePrimarioOLambda -> ParentizadaOLambda
    public static final ConjuntoDePrimeros EXPRESION_DESDE_PRIMARIO_OLAMBDA_PARENTIZADA_OLAMBDA = de(
            TipoDeToken.PAREN_A);

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    public static final ConjuntoDePrimeros EXPRESION_CONDICIONAL_EXPRESION_COMPUESTA = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // RestoCondicional -> ? Expresion : ExpresionCondicionalOLambda
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

    // ColaTrasOperando -> ColaTrasOperandoSinAsignacion RestoExpresion
    public static final ConjuntoDePrimeros COLA_TRAS_OPERANDO_COLA_TRAS_OPERANDO_SIN_ASIGNACION = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR,
            TipoDeToken.OP_MENOR_IGUAL, TipoDeToken.OP_ASIGNACION, TipoDeToken.OP_IGUAL,
            TipoDeToken.OP_MAYOR, TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.OP_OR);

    // ColaTrasOperandoSinAsignacion -> RestoExpresionCompuesta RestoCondicional
    public static final ConjuntoDePrimeros COLA_TRAS_OPERANDO_SIN_ASIGNACION_RESTO_EXPRESION_COMPUESTA = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR,
            TipoDeToken.OP_MENOR_IGUAL, TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR,
            TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.OP_OR);

    // ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
    public static final ConjuntoDePrimeros COLA_DE_EXPRESION_DESDE_PRIMARIO_RESTO_REFERENCIA = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_INCREMENTO, TipoDeToken.OP_RESTA, TipoDeToken.OP_DECREMENTO,
            TipoDeToken.PUNTO, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR, TipoDeToken.OP_MENOR_IGUAL,
            TipoDeToken.OP_ASIGNACION, TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR,
            TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.CORCHETE_A,
            TipoDeToken.OP_OR);

    // ColaDesdePrimarioSinAsignacion -> RestoReferencia PosfijoOpcional ColaTrasOperandoSinAsignacion
    public static final ConjuntoDePrimeros COLA_DESDE_PRIMARIO_SIN_ASIGNACION_RESTO_REFERENCIA = de(
            TipoDeToken.OP_DISTINTO, TipoDeToken.OP_MODULO, TipoDeToken.OP_AND, TipoDeToken.OP_MULT,
            TipoDeToken.OP_SUMA, TipoDeToken.OP_INCREMENTO, TipoDeToken.OP_RESTA, TipoDeToken.OP_DECREMENTO,
            TipoDeToken.PUNTO, TipoDeToken.OP_DIV, TipoDeToken.OP_MENOR, TipoDeToken.OP_MENOR_IGUAL,
            TipoDeToken.OP_IGUAL, TipoDeToken.OP_MAYOR, TipoDeToken.OP_MAYOR_IGUAL,
            TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.CORCHETE_A, TipoDeToken.OP_OR);

    private PrimerosDeExpresiones() {
    }
}
