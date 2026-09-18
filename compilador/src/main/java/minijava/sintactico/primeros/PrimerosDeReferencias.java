package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// Las referencias, los primarios y el encadenamiento.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeReferencias {

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

    // PrimarioSinIdClase -> ExpresionParentizada
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_EXPRESION_PARENTIZADA = de(
            TipoDeToken.PAREN_A);

    // PrimarioSinIdClase -> AccesoVarOLlamadaMetodo
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA_METODO = de(
            TipoDeToken.ID_MV);

    // PrimarioSinIdClase -> Creacion
    public static final ConjuntoDePrimeros PRIMARIO_SIN_ID_CLASE_CREACION = de(TipoDeToken.PR_NEW);

    // ExpresionParentizada -> ( Expresion )
    public static final ConjuntoDePrimeros EXPRESION_PARENTIZADA_PAREN_A = de(TipoDeToken.PAREN_A);

    // AccesoVarOLlamadaMetodo -> idMetVar AccesoOLlamada
    public static final ConjuntoDePrimeros ACCESO_VAR_OLLAMADA_METODO_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // AccesoOLlamada -> ArgsActuales
    public static final ConjuntoDePrimeros ACCESO_OLLAMADA_ARGS_ACTUALES = de(TipoDeToken.PAREN_A);

    // AccesoOLlamada -> EPSILON
    public static final ConjuntoDePrimeros ACCESO_OLLAMADA = de();

    private PrimerosDeReferencias() {
    }
}
