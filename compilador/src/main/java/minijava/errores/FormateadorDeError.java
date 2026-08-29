package minijava.errores;

import minijava.config.ConfiguracionDelCompilador;

import java.util.List;

public final class FormateadorDeError {

    private static final int ANCHO_DE_TABULACION = 8;

    private final ConfiguracionDelCompilador configuracion;

    public FormateadorDeError(ConfiguracionDelCompilador configuracion) {
        this.configuracion = configuracion;
    }

    public List<String> formatear(ExcepcionLexica excepcion) {
        String codigo = "[Error:" + excepcion.getLexemaOfensivo() + "|" + excepcion.getLinea() + "]";
        if (!configuracion.reporteElegante()) {
            return List.of(codigo);
        }
        String encabezado = "Error Léxico en línea " + excepcion.getLinea()
                + ", columna " + excepcion.getColumna() + ": "
                + excepcion.getTipo().describir(excepcion.getLexemaOfensivo());
        String lineaFuente = excepcion.getLineaFuente();
        int indiceDelError = Math.min(excepcion.getColumna() - 1, lineaFuente.length());
        String detalle = "Detalle: " + renderizarLineaFuente(lineaFuente, indiceDelError);
        int columnaVisual = columnaVisualAntesDe(lineaFuente, indiceDelError);
        String caret = " ".repeat("Detalle: ".length() + Math.max(0, columnaVisual)) + "^";
        // El renglón en blanco antes del código es el de Pautas, slide 25.
        return List.of(encabezado, detalle, caret, "", codigo);
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
            } else if (i == indiceDelError && debeEscaparseEnDetalle(caracter)) {
                resultado.append(String.format("\\u%04x", (int) caracter));
                columna += 6;
            } else {
                resultado.append(caracter);
                columna += anchoVisual(caracter);
            }
        }
        return resultado.toString();
    }

    private static boolean debeEscaparseEnDetalle(char caracter) {
        return esMarcaCombinante(caracter)
                || Character.isISOControl(caracter)
                || Character.getType(caracter) == Character.FORMAT
                || (Character.isSpaceChar(caracter) && caracter != ' ');
    }

    private static int columnaVisualAntesDe(String texto, int indice) {
        int columna = 0;
        for (int i = 0; i < indice; i++) {
            char caracter = texto.charAt(i);
            if (caracter == '\t') {
                columna += ANCHO_DE_TABULACION - columna % ANCHO_DE_TABULACION;
            } else {
                columna += anchoVisual(caracter);
            }
        }
        return columna;
    }

    private static int anchoVisual(char caracter) {
        if (esMarcaCombinante(caracter) || Character.isISOControl(caracter)) {
            return 0;
        }
        return esCaracterAnchoDoble(caracter) ? 2 : 1;
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
