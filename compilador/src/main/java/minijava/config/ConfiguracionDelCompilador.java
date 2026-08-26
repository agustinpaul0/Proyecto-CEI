package minijava.config;

// Los cuatro logros que CAMBIAN el comportamiento observable del compilador,
// cada uno detrás de su interruptor. Existen para poder demostrar que la
// entrega cumple tanto el comportamiento base que pide la consigna (reportar el
// primer error y terminar; no reconocer floats) como los logros que lo
// modifican — sin el interruptor, el comportamiento base quedaría sepultado y
// no habría forma de testearlo.
//
// Los dos primeros los consultan los estados del AF, porque cambian el lenguaje
// reconocido; los otros dos viven fuera del léxico (el formateador de errores y
// el Módulo Principal).
public record ConfiguracionDelCompilador(
        boolean floatsHabilitados,
        boolean unicodeHabilitado,
        boolean reporteElegante,
        boolean multiDeteccionDeErrores) {

    public static final ConfiguracionDelCompilador DEFAULT =
            new ConfiguracionDelCompilador(true, true, true, true);
}
