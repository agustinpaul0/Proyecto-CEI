package minijava.sintactico.primeros;

import minijava.lexico.tokens.TipoDeToken;

import static minijava.sintactico.primeros.ConjuntoDePrimeros.de;

// GENERADO por herramientas/primeros_a_java.py desde
// gramatica/minijava-ll1-logros.g. No editar a mano: se regenera.
//
// Los miembros de una clase y los metodos de una interfaz.
//
// Una constante por produccion, con su produccion citada al lado, para que
// el analizador se lea contra la gramatica del informe renglon por renglon.
// Las producciones que existen por un logro se emiten igual: el analizador
// consulta el logro por su cuenta antes de tomar la rama.
public final class PrimerosDeMiembros {

    // Miembro -> VisibilidadOpcional MiembroSinVisibilidad
    public static final ConjuntoDePrimeros MIEMBRO_VISIBILIDAD_OPCIONAL = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_PRIVATE, TipoDeToken.PR_PUBLIC,
            TipoDeToken.PR_STATIC, TipoDeToken.PR_VOID);

    // Miembro -> MiembroSinVisibilidad
    public static final ConjuntoDePrimeros MIEMBRO_MIEMBRO_SIN_VISIBILIDAD = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.ID_CLASE,
            TipoDeToken.ID_GEN, TipoDeToken.PR_INT, TipoDeToken.PR_STATIC, TipoDeToken.PR_VOID);

    // Miembro -> Constructor
    public static final ConjuntoDePrimeros MIEMBRO_CONSTRUCTOR = de(TipoDeToken.PR_PUBLIC);

    // VisibilidadOpcional -> public
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // VisibilidadOpcional -> private
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL_PRIVATE = de(TipoDeToken.PR_PRIVATE);

    // VisibilidadOpcional -> EPSILON
    public static final ConjuntoDePrimeros VISIBILIDAD_OPCIONAL = de();

    // Constructor -> public idClase CuerpoDeMetodo
    public static final ConjuntoDePrimeros CONSTRUCTOR_PUBLIC = de(TipoDeToken.PR_PUBLIC);

    // MiembroSinVisibilidad -> static TipoMetodo Metodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_STATIC = de(TipoDeToken.PR_STATIC);

    // MiembroSinVisibilidad -> void Metodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_VOID = de(TipoDeToken.PR_VOID);

    // MiembroSinVisibilidad -> TipoPrimitivo DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_TIPO_PRIMITIVO = de(
            TipoDeToken.PR_BOOLEAN, TipoDeToken.PR_CHAR, TipoDeToken.PR_FLOAT, TipoDeToken.PR_INT);

    // MiembroSinVisibilidad -> idGen DimensionesOpcionales idMetVar AtributoOMetodo
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_ID_GEN = de(TipoDeToken.ID_GEN);

    // MiembroSinVisibilidad -> idClase MiembroDesdeIdClase
    public static final ConjuntoDePrimeros MIEMBRO_SIN_VISIBILIDAD_ID_CLASE = de(TipoDeToken.ID_CLASE);

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
