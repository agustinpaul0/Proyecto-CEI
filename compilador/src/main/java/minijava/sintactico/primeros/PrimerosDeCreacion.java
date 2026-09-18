package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// Las expresiones de creacion.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeCreacion {

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

    // CreacionDesdeIdClase -> ArregloOInstanciaDeClase
    public static final ConjuntoDePrimeros CREACION_DESDE_ID_CLASE_ARREGLO_OINSTANCIA_DE_CLASE = de(
            TipoDeToken.PAREN_A, TipoDeToken.CORCHETE_A);

    // GenericoDeCreacion -> > ArgsActuales
    public static final ConjuntoDePrimeros GENERICO_DE_CREACION_MAYOR = de(TipoDeToken.OP_MAYOR);

    // GenericoDeCreacion -> InstanciadoOParametrico > ArregloOInstanciaDeClase
    public static final ConjuntoDePrimeros GENERICO_DE_CREACION_INSTANCIADO_OPARAMETRICO = de(
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

    // RestoCreacionDeArreglo -> ] DimensionesOpcionales InicializadorDeArreglo
    public static final ConjuntoDePrimeros RESTO_CREACION_DE_ARREGLO_CORCHETE_C = de(TipoDeToken.CORCHETE_C);

    // DimensionesConTamanio -> [ Expresion ] DimensionesConTamanioOpcional
    public static final ConjuntoDePrimeros DIMENSIONES_CON_TAMANIO_CORCHETE_A = de(TipoDeToken.CORCHETE_A);

    // DimensionesConTamanioOpcional -> DimensionesConTamanio
    public static final ConjuntoDePrimeros DIMENSIONES_CON_TAMANIO_OPCIONAL_DIMENSIONES_CON_TAMANIO = de(
            TipoDeToken.CORCHETE_A);

    // DimensionesConTamanioOpcional -> EPSILON
    public static final ConjuntoDePrimeros DIMENSIONES_CON_TAMANIO_OPCIONAL = de();

    // InicializadorDeArreglo -> { ListaElementosOpcional }
    public static final ConjuntoDePrimeros INICIALIZADOR_DE_ARREGLO_LLAVE_A = de(TipoDeToken.LLAVE_A);

    // ListaElementosOpcional -> ListaElementos
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_OPCIONAL_LISTA_ELEMENTOS = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE, TipoDeToken.LLAVE_A);

    // ListaElementosOpcional -> ,
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_OPCIONAL_COMA = de(TipoDeToken.COMA);

    // ListaElementosOpcional -> EPSILON
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_OPCIONAL = de();

    // ListaElementos -> ElementoInicializador RestoListaElementos
    public static final ConjuntoDePrimeros LISTA_ELEMENTOS_ELEMENTO_INICIALIZADOR = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE, TipoDeToken.LLAVE_A);

    // RestoListaElementos -> , ElementosTrasComa
    public static final ConjuntoDePrimeros RESTO_LISTA_ELEMENTOS_COMA = de(TipoDeToken.COMA);

    // RestoListaElementos -> EPSILON
    public static final ConjuntoDePrimeros RESTO_LISTA_ELEMENTOS = de();

    // ElementosTrasComa -> ElementoInicializador RestoListaElementos
    public static final ConjuntoDePrimeros ELEMENTOS_TRAS_COMA_ELEMENTO_INICIALIZADOR = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE, TipoDeToken.LLAVE_A);

    // ElementosTrasComa -> EPSILON
    public static final ConjuntoDePrimeros ELEMENTOS_TRAS_COMA = de();

    // ElementoInicializador -> Expresion
    public static final ConjuntoDePrimeros ELEMENTO_INICIALIZADOR_EXPRESION = de(
            TipoDeToken.OP_NEGACION, TipoDeToken.PAREN_A, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA,
            TipoDeToken.CHAR_LITERAL, TipoDeToken.PR_FALSE, TipoDeToken.FLOAT_LITERAL, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_MV, TipoDeToken.INT_LITERAL, TipoDeToken.PR_NEW, TipoDeToken.PR_NULL,
            TipoDeToken.LIT_STRING, TipoDeToken.PR_THIS, TipoDeToken.PR_TRUE);

    // ElementoInicializador -> InicializadorDeArreglo
    public static final ConjuntoDePrimeros ELEMENTO_INICIALIZADOR_INICIALIZADOR_DE_ARREGLO = de(
            TipoDeToken.LLAVE_A);

    private PrimerosDeCreacion() {
    }
}
