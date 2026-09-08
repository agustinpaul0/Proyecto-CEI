package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-logros.g. No editar a mano: se regenera.
//
// La estructura del fuente: clases, interfaces y su cabecera.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
// Las producciones que existen por un logro se emiten igual: el analizador
// consulta el logro por su cuenta antes de tomar la rama.
public final class PrimerosDeClases {

    // Inicial -> ListaClases eof
    public static final ConjuntoDePrimeros INICIAL_LISTA_CLASES = de(
            TipoDeToken.PR_CLASS, TipoDeToken.EOF, TipoDeToken.PR_INTERFACE);

    // ListaClases -> Clase ListaClases
    public static final ConjuntoDePrimeros LISTA_CLASES_CLASE = de(TipoDeToken.PR_CLASS);

    // ListaClases -> Interfaz ListaClases
    public static final ConjuntoDePrimeros LISTA_CLASES_INTERFAZ = de(TipoDeToken.PR_INTERFACE);

    // ListaClases -> EPSILON
    public static final ConjuntoDePrimeros LISTA_CLASES = de();

    // Clase -> class idClase GenericidadOpcional HerenciaOpcional { ListaMiembros }
    public static final ConjuntoDePrimeros CLASE_CLASS = de(TipoDeToken.PR_CLASS);

    // Interfaz -> interface idClase GenericidadOpcional ExtensionOpcional { ListaMetodosInterfaz }
    public static final ConjuntoDePrimeros INTERFAZ_INTERFACE = de(TipoDeToken.PR_INTERFACE);

    // GenericidadOpcional -> < idGen >
    public static final ConjuntoDePrimeros GENERICIDAD_OPCIONAL_MENOR = de(TipoDeToken.OP_MENOR);

    // GenericidadOpcional -> EPSILON
    public static final ConjuntoDePrimeros GENERICIDAD_OPCIONAL = de();

    // HerenciaOpcional -> extends TipoReferencia
    public static final ConjuntoDePrimeros HERENCIA_OPCIONAL_EXTENDS = de(TipoDeToken.PR_EXTENDS);

    // HerenciaOpcional -> implements TipoReferencia
    public static final ConjuntoDePrimeros HERENCIA_OPCIONAL_IMPLEMENTS = de(TipoDeToken.PR_IMPLEMENTS);

    // HerenciaOpcional -> EPSILON
    public static final ConjuntoDePrimeros HERENCIA_OPCIONAL = de();

    // ExtensionOpcional -> extends TipoReferencia
    public static final ConjuntoDePrimeros EXTENSION_OPCIONAL_EXTENDS = de(TipoDeToken.PR_EXTENDS);

    // ExtensionOpcional -> EPSILON
    public static final ConjuntoDePrimeros EXTENSION_OPCIONAL = de();

    // ListaMiembros -> Miembro ListaMiembros
    public static final ConjuntoDePrimeros LISTA_MIEMBROS_MIEMBRO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PRIVATE, TipoDeToken.PR_PUBLIC,
            TipoDeToken.PR_STATIC, TipoDeToken.PR_VOID);

    // ListaMiembros -> EPSILON
    public static final ConjuntoDePrimeros LISTA_MIEMBROS = de();

    // ListaMetodosInterfaz -> MetodoInterfaz ListaMetodosInterfaz
    public static final ConjuntoDePrimeros LISTA_METODOS_INTERFAZ_METODO_INTERFAZ = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PUBLIC, TipoDeToken.PR_VOID);

    // ListaMetodosInterfaz -> EPSILON
    public static final ConjuntoDePrimeros LISTA_METODOS_INTERFAZ = de();

    private PrimerosDeClases() {
    }
}
