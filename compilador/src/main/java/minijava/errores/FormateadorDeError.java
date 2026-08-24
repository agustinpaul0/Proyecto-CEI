package minijava.errores;

import java.util.List;

// Reporte de Error Elegante (logro): línea completa del fuente + '^' apuntando
// a la columna del carácter ofensivo (Pautas Etapa 1, slide 43).
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
