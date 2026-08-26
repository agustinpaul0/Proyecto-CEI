package minijava.errores;

// Un valor por punto de falla del léxico, con la razón que se imprime en el
// encabezado del Reporte de Error Elegante. Existe por dos motivos:
//
//  1. Permite que el reporte diga QUÉ construcción falló ("charLiteral sin
//     cerrar") en vez de una única frase genérica para los 14 puntos de
//     error, que era lo que había antes.
//  2. Permite que los tests validen un error por TIPO y localización en vez
//     de por el texto del mensaje, que es lo que exige agents/testing.md y
//     hasta ahora era imposible.
//
// El código de contrato [Error:Lexema|NroLinea] NO se ve afectado: el tipo
// sólo cambia el encabezado legible, que es un agregado propio del proyecto.
public enum TipoDeErrorLexico {

    // Texto tomado literal del ejemplo de Pautas Etapa 1 (slide 43). Es el
    // único mensaje que la cátedra fija, y no se toca.
    CARACTER_DESCONOCIDO("%s no es un símbolo válido"),

    CHAR_VACIO("charLiteral vacío: %s"),
    CHAR_MAL_FORMADO("charLiteral mal formado, contiene más de un carácter: %s"),
    CHAR_SIN_CERRAR("charLiteral sin cerrar: %s"),

    // La única con plantilla de dos datos: el lexema armado que exige Pautas
    // ("a\q) no ES la secuencia de escape, así que nombrarla aparte evita
    // que el mensaje se lea al revés. Ver describir(lexema, fragmento).
    STRING_ESCAPE_INVALIDO("litString con secuencia de escape inválida: %s",
            "litString con secuencia de escape inválida %s: %s"),
    STRING_SIN_CERRAR("litString sin cerrar: %s"),

    UNICODE_MAL_FORMADO("escape unicode mal formado, se esperaban 4 dígitos hexadecimales: %s"),

    COMENTARIO_SIN_CERRAR("comentario multilínea sin cerrar: %s"),

    ENTERO_DEMASIADO_LARGO("intLiteral de más de 9 dígitos: %s"),
    OPERADOR_INCOMPLETO("operador incompleto, se esperaba && o ||: %s"),
    EXPONENTE_SIN_DIGITOS("exponente sin dígitos: %s");

    private final String plantilla;
    // Null salvo donde el error tiene, además del lexema armado, un fragmento
    // ofensivo más chico que vale la pena nombrar. Sus dos %s son, en orden,
    // (fragmento, lexema).
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

    // Se degrada al mensaje de un solo dato si no hay fragmento, o si este
    // tipo no tiene ninguno que nombrar: nunca imprime un "null".
    public String describir(String lexema, String fragmentoOfensivo) {
        if (fragmentoOfensivo == null || plantillaConFragmento == null) {
            return describir(lexema);
        }
        return plantillaConFragmento.formatted(fragmentoOfensivo, lexema);
    }
}
