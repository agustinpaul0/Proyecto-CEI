package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// Los Primeros de cada producción de la gramática LL(1) final, uno por constante
// y con la producción citada al lado, para que el código se lea contra el
// informe renglón por renglón.
//
// Se contrastan con:
//     python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g
public final class PrimerosDelNucleo {

    // ListaClases -> Clase ListaClases
    public static final ConjuntoDePrimeros CLASE = de(TipoDeToken.PR_CLASS);

    // ListaClases -> Interfaz ListaClases
    public static final ConjuntoDePrimeros INTERFAZ = de(TipoDeToken.PR_INTERFACE);

    // GenericidadOpcional -> < idGen >
    public static final ConjuntoDePrimeros GENERICIDAD = de(TipoDeToken.OP_MENOR);

    // HerenciaOpcional -> extends TipoReferencia
    public static final ConjuntoDePrimeros HERENCIA_POR_EXTENDS = de(TipoDeToken.PR_EXTENDS);

    // HerenciaOpcional -> implements TipoReferencia
    public static final ConjuntoDePrimeros HERENCIA_POR_IMPLEMENTS = de(TipoDeToken.PR_IMPLEMENTS);

    // TipoGenericoOpcional -> < ArgumentoDeTipo >
    public static final ConjuntoDePrimeros TIPO_GENERICO = de(TipoDeToken.OP_MENOR);

    // ArgumentoDeTipo -> idGen
    public static final ConjuntoDePrimeros ARGUMENTO_DE_TIPO_PARAMETRO = de(TipoDeToken.ID_GEN);

    // ArgumentoDeTipo -> idClase TipoGenericoOpcional   [+generico]
    // ArgumentoDeTipo -> idClase                        [-generico]
    public static final ConjuntoDePrimeros ARGUMENTO_DE_TIPO_CLASE = de(TipoDeToken.ID_CLASE);

    public static final ConjuntoDePrimeros ARGUMENTO_DE_TIPO =
            ARGUMENTO_DE_TIPO_PARAMETRO.unido(ARGUMENTO_DE_TIPO_CLASE);

    private PrimerosDelNucleo() {
    }
}
