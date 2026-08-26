package minijava.errores;

import minijava.config.ConfiguracionDelCompilador;

import java.util.List;

public final class FormateadorDeError {

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
        String detalle = "Detalle: " + excepcion.getLineaFuente();
        String caret = " ".repeat("Detalle: ".length() + excepcion.getColumna() - 1) + "^";
        return List.of(encabezado, detalle, caret, codigo);
    }
}
