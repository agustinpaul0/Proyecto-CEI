package minijava.errores;

// Un invisible dentro de un lexema deja el mensaje de error ilegible —o, si es un
// salto, parte [Error:Lexema|NroLinea] en dos y rompe el formato de un elemento
// por línea que espera la cátedra—. Sólo para errores: el lexema de un token va crudo.
public final class EscapadorDeNoImprimibles {

    private EscapadorDeNoImprimibles() {
    }

    public static String escapar(String texto) {
        if (texto == null) {
            return null;
        }
        StringBuilder escapado = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '\t' -> escapado.append("\\t");
                case '\n' -> escapado.append("\\n");
                case '\r' -> escapado.append("\\r");
                case '\f' -> escapado.append("\\f");
                case '\b' -> escapado.append("\\b");
                default -> {
                    if (esInvisible(texto, i)) {
                        escapado.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        escapado.append(c);
                    }
                }
            }
        }
        return escapado.toString();
    }

    // Controles, formato (el BOM), espacios que no son el común, surrogados
    // SUELTOS y marcas combinantes SIN BASE. Un par bien formado —un emoji— y una
    // marca sobre su letra —un acento descompuesto— se dejan crudos: se ven.
    private static boolean esInvisible(String texto, int posicion) {
        char c = texto.charAt(posicion);
        if (Character.isISOControl(c)) {
            return true;
        }
        if (Character.isHighSurrogate(c)) {
            return posicion + 1 >= texto.length() || !Character.isLowSurrogate(texto.charAt(posicion + 1));
        }
        if (Character.isLowSurrogate(c)) {
            return posicion == 0 || !Character.isHighSurrogate(texto.charAt(posicion - 1));
        }
        if (Character.isSpaceChar(c)) {
            return c != ' ';
        }
        // Una marca combinante se dibuja SOBRE el carácter anterior: pegada a su
        // base se ve, y suelta no se ve nada. El caso real es el selector de
        // variación U+FE0F de un emoji, que queda solo en el lexema de un error.
        if (esMarcaCombinante(c)) {
            return posicion == 0 || !tieneBaseVisible(texto.charAt(posicion - 1));
        }
        return Character.getType(c) == Character.FORMAT;
    }

    private static boolean esMarcaCombinante(char c) {
        int tipo = Character.getType(c);
        return tipo == Character.NON_SPACING_MARK
                || tipo == Character.COMBINING_SPACING_MARK
                || tipo == Character.ENCLOSING_MARK;
    }

    // Sirve de base lo que se dibuja por sí solo: ni un control (que se va a
    // escapar) ni otra marca (que tampoco tiene forma propia).
    private static boolean tieneBaseVisible(char anterior) {
        return !Character.isISOControl(anterior) && !esMarcaCombinante(anterior);
    }
}
