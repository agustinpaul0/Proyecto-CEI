package minijava.lexico.errores;

import java.util.List;

public final class FormateadorDeError {

    private static final int ANCHO_DE_TABULACION = 8;

    public List<String> formatear(ExcepcionLexica excepcion) {
        String codigo = "[Error:" + excepcion.getLexemaOfensivo() + "|" + excepcion.getLinea() + "]";
        String encabezado = "Error Léxico en línea " + excepcion.getLinea()
                + ", columna " + excepcion.getColumna() + ": "
                + excepcion.getTipo().describir(excepcion.getLexemaOfensivo());
        String lineaFuente = excepcion.getLineaFuente();
        int indiceDelError = Math.min(excepcion.getColumna() - 1, lineaFuente.length());
        String detalle = "Detalle: " + renderizarLineaFuente(lineaFuente, indiceDelError);
        int columnaVisual = columnaVisualAntesDe(lineaFuente, indiceDelError);
        String caret = " ".repeat("Detalle: ".length() + Math.max(0, columnaVisual)) + "^";
        return List.of(encabezado, detalle, caret, codigo);
    }

    private static String renderizarLineaFuente(String texto, int indiceDelError) {
        StringBuilder resultado = new StringBuilder(texto.length());
        int columna = 0;
        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);
            if (caracter == '\t') {
                int espacios = ANCHO_DE_TABULACION - columna % ANCHO_DE_TABULACION;
                resultado.append(" ".repeat(espacios));
                columna += espacios;
            } else if (esParObjetivo(texto, i, indiceDelError)) {
                char bajo = texto.charAt(i + 1);
                resultado.append(representacionVisible(caracter));
                resultado.append(representacionVisible(bajo));
                columna += representacionVisible(caracter).length()
                        + representacionVisible(bajo).length();
                i++;
            } else if (debeEscaparseEnDetalle(texto, i)) {
                String representacion = representacionVisible(caracter);
                resultado.append(representacion);
                columna += representacion.length();
            } else {
                resultado.append(caracter);
                columna += anchoVisual(texto, i);
            }
        }
        return resultado.toString();
    }

    private static String representacionVisible(char caracter) {
        return switch (caracter) {
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\f' -> "\\f";
            case '\b' -> "\\b";
            default -> String.format("\\u%04x", (int) caracter);
        };
    }

    private static boolean debeEscaparseEnDetalle(String texto, int indice) {
        char caracter = texto.charAt(indice);
        return esMarcaCombinante(caracter)
                || Character.isISOControl(caracter)
                || Character.getType(caracter) == Character.FORMAT
                || (Character.isSpaceChar(caracter) && caracter != ' ')
                || (Character.isSurrogate(caracter) && !esParteDeParSurrogado(texto, indice));
    }

    private static int columnaVisualAntesDe(String texto, int indice) {
        int columna = 0;
        for (int i = 0; i < indice; i++) {
            char caracter = texto.charAt(i);
            if (caracter == '\t') {
                columna += ANCHO_DE_TABULACION - columna % ANCHO_DE_TABULACION;
            } else if (esParObjetivo(texto, i, indice)) {
                columna += representacionVisible(caracter).length();
            } else if (debeEscaparseEnDetalle(texto, i)) {
                columna += representacionVisible(caracter).length();
            } else {
                columna += anchoVisual(texto, i);
            }
        }
        return columna;
    }

    private static boolean esParObjetivo(String texto, int indice, int indiceDelError) {
        return Character.isHighSurrogate(texto.charAt(indice))
                && indice + 1 < texto.length()
                && Character.isLowSurrogate(texto.charAt(indice + 1))
                && (indice == indiceDelError || indice + 1 == indiceDelError);
    }

    private static int anchoVisual(String texto, int indice) {
        char caracter = texto.charAt(indice);
        if (esParteDeParSurrogado(texto, indice)) {
            if (Character.isLowSurrogate(caracter)) {
                return 0;
            }
            return 2;
        }
        if (esMarcaCombinante(caracter) || Character.isISOControl(caracter)
                || Character.getType(caracter) == Character.FORMAT) {
            return 0;
        }
        return esCaracterAnchoDoble(caracter) ? 2 : 1;
    }

    private static boolean esParteDeParSurrogado(String texto, int indice) {
        char caracter = texto.charAt(indice);
        return Character.isHighSurrogate(caracter)
                && indice + 1 < texto.length()
                && Character.isLowSurrogate(texto.charAt(indice + 1))
                || Character.isLowSurrogate(caracter)
                && indice > 0
                && Character.isHighSurrogate(texto.charAt(indice - 1));
    }

    private static boolean esMarcaCombinante(char caracter) {
        int tipo = Character.getType(caracter);
        return tipo == Character.NON_SPACING_MARK
                || tipo == Character.COMBINING_SPACING_MARK
                || tipo == Character.ENCLOSING_MARK
                // Algunas versiones de Unicode de la JVM aún no clasifican U+20CC.
                || caracter == '\u20CC';
    }

    private static boolean esCaracterAnchoDoble(char caracter) {
        return caracter >= 0x1100 && (caracter <= 0x115F
                || caracter == 0x2329 || caracter == 0x232A
                || (caracter >= 0x2E80 && caracter <= 0xA4CF)
                || (caracter >= 0xAC00 && caracter <= 0xD7A3)
                || (caracter >= 0xF900 && caracter <= 0xFAFF)
                || (caracter >= 0xFE10 && caracter <= 0xFE19)
                || (caracter >= 0xFF01 && caracter <= 0xFF60)
                || (caracter >= 0xFFE0 && caracter <= 0xFFE6));
    }
}
