package minijava.errores;

import java.util.List;

public final class FormateadorDeError {

    public List<String> formatear(ExcepcionLexica excepcion) {
        String encabezado = "Error Léxico en línea " + excepcion.getLinea()
                + ", columna " + excepcion.getColumna() + ": "
                + excepcion.getLexemaOfensivo() + " no es un símbolo válido";
        String detalle = "Detalle: " + excepcion.getLineaFuente();
        String caret = " ".repeat("Detalle: ".length() + excepcion.getColumna() - 1) + "^";
        String codigo = "[Error:" + excepcion.getLexemaOfensivo() + "|" + excepcion.getLinea() + "]";
        return List.of(encabezado, detalle, caret, codigo);
    }
}
