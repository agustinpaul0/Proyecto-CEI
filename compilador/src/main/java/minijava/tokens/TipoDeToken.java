package minijava.tokens;

// Los 56 tokens de MiniJava. El String de cada valor NO es decorativo: es el
// nombre exacto que se imprime en (NombreToken,Lexema,NroLinea), que es el
// formato de salida que fija la cátedra. Cambiar uno cambia la salida del
// compilador.
public enum TipoDeToken {
    PR_CLASS("pr_class"), PR_EXTENDS("pr_extends"), PR_INTERFACE("pr_interface"),
    PR_IMPLEMENTS("pr_implements"), PR_STATIC("pr_static"), PR_BOOLEAN("pr_boolean"),
    PR_CHAR("pr_char"), PR_INT("pr_int"), PR_VOID("pr_void"), PR_PUBLIC("pr_public"),
    PR_IF("pr_if"), PR_ELSE("pr_else"), PR_WHILE("pr_while"), PR_RETURN("pr_return"),
    PR_VAR("pr_var"), PR_THIS("pr_this"), PR_NEW("pr_new"), PR_NULL("pr_null"),
    PR_TRUE("pr_true"), PR_FALSE("pr_false"), PR_FLOAT("pr_float"),

    ID_CLASE("idClase"), ID_GEN("idGen"), ID_MV("idMV"),

    INT_LITERAL("intLiteral"), CHAR_LITERAL("charLiteral"),
    LIT_STRING("litString"), FLOAT_LITERAL("floatLiteral"),

    PAREN_A("parenA"), PAREN_C("parenC"), LLAVE_A("llaveA"), LLAVE_C("llaveC"),
    CORCHETE_A("corcheteA"), CORCHETE_C("corcheteC"), PUNTO_Y_COMA("puntoYComa"),
    COMA("coma"), PUNTO("punto"), DOS_PUNTOS("dosPuntos"),

    OP_MAYOR("op_mayor"), OP_MENOR("op_menor"), OP_NEGACION("op_negacion"),
    OP_ASIGNACION("op_asignacion"), OP_IGUAL("op_igual"), OP_MAYOR_IGUAL("op_mayorIgual"),
    OP_MENOR_IGUAL("op_menorIgual"), OP_DISTINTO("op_distinto"), OP_AND("op_and"),
    OP_OR("op_or"), OP_MODULO("op_modulo"), OP_SUMA("op+"), OP_RESTA("op-"),
    OP_MULT("op_mult"), OP_DIV("op_div"), OP_INCREMENTO("op_incremento"),
    OP_DECREMENTO("op_decremento"),

    EOF("EOF");

    private final String nombre;

    TipoDeToken(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
