package minijava.errores;

// Un valor por punto de falla del léxico, con la razón que se imprime en el
// encabezado del Reporte de Error Elegante. Sólo afecta a ese encabezado: el
// código de contrato [Error:Lexema|NroLinea] no lleva el tipo.
public enum TipoDeErrorLexico {

    // Único mensaje que fija la cátedra (Pautas Etapa 1, slide 43): no se toca.
    CARACTER_DESCONOCIDO("%s no es un símbolo válido"),

    CHAR_VACIO("charLiteral vacío: %s"),
    CHAR_MAL_FORMADO("charLiteral mal formado, contiene más de un carácter: %s"),
    CHAR_SIN_CERRAR("charLiteral sin cerrar: %s"),

    // La única con plantilla de dos datos: el lexema armado que exige Pautas
    // ("a\q) no ES la secuencia de escape, y nombrarla aparte evita que el
    // mensaje se lea al revés.
    STRING_ESCAPE_INVALIDO("litString con secuencia de escape inválida: %s",
            "litString con secuencia de escape inválida %s: %s"),
    STRING_SIN_CERRAR("litString sin cerrar: %s"),

    UNICODE_MAL_FORMADO("escape unicode mal formado, se esperaban 4 dígitos hexadecimales: %s"),

    COMENTARIO_SIN_CERRAR("comentario multilínea sin cerrar: %s"),

    ENTERO_DEMASIADO_LARGO("intLiteral de más de 9 dígitos: %s"),
    OPERADOR_INCOMPLETO("operador incompleto, se esperaba && o ||: %s"),
    EXPONENTE_SIN_DIGITOS("exponente sin dígitos: %s");

    private final String plantilla;
    // Sus dos %s son, en orden, (fragmento, lexema).
    private final String plantillaConFragmento;

    TipoDeErrorLexico(String plantilla) {
        this(plantilla, null);
    }

    TipoDeErrorLexico(String plantilla, String plantillaConFragmento) {
        this.plantilla = plantilla;
        this.plantillaConFragmento = plantillaConFragmento;
    }

    public String describir(String lexema) {
        return plantilla.formatted(lexema);
    }

    // Se degrada al mensaje de un dato si no hay fragmento: nunca imprime "null".
    public String describir(String lexema, String fragmentoOfensivo) {
        if (fragmentoOfensivo == null || plantillaConFragmento == null) {
            return describir(lexema);
        }
        return plantillaConFragmento.formatted(fragmentoOfensivo, lexema);
    }
}
