package minijava.errores;

// Un valor por punto de falla, con la razón que va en el encabezado del reporte
// elegante. El código [Error:Lexema|NroLinea] no lleva el tipo.
public enum TipoDeErrorLexico {

    // Único mensaje que fija la cátedra (Pautas Etapa 1, slide 43): no se toca.
    CARACTER_DESCONOCIDO("%s no es un símbolo válido"),

    CHAR_VACIO("charLiteral vacío: %s"),
    CHAR_MAL_FORMADO("charLiteral mal formado, contiene más de un carácter: %s"),
    CHAR_SIN_CERRAR("charLiteral sin cerrar: %s"),

    STRING_SIN_CERRAR("litString sin cerrar: %s"),

    UNICODE_MAL_FORMADO("escape unicode mal formado, se esperaban 4 dígitos hexadecimales: %s"),

    COMENTARIO_SIN_CERRAR("comentario multilínea sin cerrar: %s"),

    ENTERO_DEMASIADO_LARGO("intLiteral de más de 9 dígitos: %s"),
    OPERADOR_INCOMPLETO("operador incompleto, se esperaba && o ||: %s"),
    EXPONENTE_SIN_DIGITOS("exponente sin dígitos: %s");

    private final String plantilla;

    TipoDeErrorLexico(String plantilla) {
        this.plantilla = plantilla;
    }

    public String describir(String lexema) {
        return plantilla.formatted(lexema);
    }
}
