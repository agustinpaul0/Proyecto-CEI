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

    // Controles, formato (el BOM), espacios que no son el común, y surrogados
    // SUELTOS. Un par bien formado —un emoji— se deja crudo: se ve.
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
        return Character.getType(c) == Character.FORMAT;
    }
}
