package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-final.g. No editar a mano: se regenera.
//
// Los miembros de una clase y los metodos de una interfaz.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
public final class PrimerosDeMiembros {

    // Miembro -> Visibilidad MiembroTrasVisibilidad
    public static final ConjuntoDePrimeros MIEMBRO_VISIBILIDAD = de(
            TipoDeToken.PR_PRIVATE, TipoDeToken.PR_PUBLIC);

    // Miembro -> static VisibilidadOpcional TipoMetodo Metodo
    public static final ConjuntoDePrimeros MIEMBRO_STATIC = de(TipoDeToken.PR_STATIC);

    // Miembro -> MiembroSinModificadores
    public static final ConjuntoDePrimeros MIEMBRO_MIEMBRO_SIN_MODIFICADORES = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_VOID);

    // MiembroTrasVisibilidad -> static TipoMetodo Metodo
    public static final ConjuntoDePrimeros MIEMBRO_TRAS_VISIBILIDAD_STATIC = de(TipoDeToken.PR_STATIC);

    // MiembroTrasVisibilidad -> MiembroSinModificadores
    public static final ConjuntoDePrimeros MIEMBRO_TRAS_VISIBILIDAD_MIEMBRO_SIN_MODIFICADORES = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_VOID);

    // Visibilidad -> public
    public static final ConjuntoDePrimeros VISIBILIDAD_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // Visibilidad -> private
    public static final ConjuntoDePrimeros VISIBILIDAD_PRIVATE = de(TipoDeToken.PR_PRIVATE);

    // VisibilidadOpcional -> Visibilidad
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL_VISIBILIDAD = de(
            TipoDeToken.PR_PRIVATE, TipoDeToken.PR_PUBLIC);

    // VisibilidadOpcional -> EPSILON
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL = de();

    // MiembroSinModificadores -> void Metodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_MODIFICADORES_VOID = de(TipoDeToken.PR_VOID);

    // MiembroSinModificadores -> TipoPrimitivo DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_MODIFICADORES_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // MiembroSinModificadores -> idGen DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_MODIFICADORES_ID_GEN = de(TipoDeToken.ID_GEN);

    // MiembroSinModificadores -> idClase MiembroDesdeIdClase
    public static final ConjuntoDePrimeros MIEMBRO_SIN_MODIFICADORES_ID_CLASE = de(TipoDeToken.ID_CLASE);

    // MiembroDesdeIdClase -> CuerpoDeMetodo
    public static final ConjuntoDePrimeros MIEMBRO_DESDE_ID_CLASE_CUERPO_DE_METODO = de(TipoDeToken.PAREN_A);

    // MiembroDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL = de(
            TipoDeToken.OP_MENOR, TipoDeToken.CORCHETE_A, TipoDeToken.ID_MV);

    // AtributoOMetodo -> InicializacionDeAtributo ;
    public static final ConjuntoDePrimeros ATRIBUTO_OMETODO_INICIALIZACION_DE_ATRIBUTO = de(
            TipoDeToken.PUNTO_Y_COMA, TipoDeToken.OP_ASIGNACION);

    // AtributoOMetodo -> CuerpoDeMetodo
    public static final ConjuntoDePrimeros ATRIBUTO_OMETODO_CUERPO_DE_METODO = de(TipoDeToken.PAREN_A);

    // InicializacionDeAtributo -> = ExpresionCondicionalOLambda
    public static final ConjuntoDePrimeros INICIALIZACION_DE_ATRIBUTO_ASIGNACION = de(
            TipoDeToken.OP_ASIGNACION);

    // InicializacionDeAtributo -> EPSILON
    public static final ConjuntoDePrimeros INICIALIZACION_DE_ATRIBUTO = de();

    // Metodo -> idMetVar CuerpoDeMetodo
    public static final ConjuntoDePrimeros METODO_ID_MET_VAR = de(TipoDeToken.ID_MV);

    // CuerpoDeMetodo -> ArgsFormales Bloque
    public static final ConjuntoDePrimeros CUERPO_DE_METODO_ARGS_FORMALES = de(TipoDeToken.PAREN_A);

    // MetodoInterfaz -> PublicOpcional TipoMetodo idMetVar ArgsFormales ;
    public static final ConjuntoDePrimeros METODO_INTERFAZ_PUBLIC_OPCIONAL = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PUBLIC, TipoDeToken.PR_VOID);

    // PublicOpcional -> public
    public static final ConjuntoDePrimeros PUBLIC_OPCIONAL_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // PublicOpcional -> EPSILON
    public static final ConjuntoDePrimeros PUBLIC_OPCIONAL = de();

    // TipoMetodo -> Tipo
    public static final ConjuntoDePrimeros TIPO_METODO_TIPO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT);

    // TipoMetodo -> void
    public static final ConjuntoDePrimeros TIPO_METODO_VOID = de(TipoDeToken.PR_VOID);

    private PrimerosDeMiembros() {
    }
}
