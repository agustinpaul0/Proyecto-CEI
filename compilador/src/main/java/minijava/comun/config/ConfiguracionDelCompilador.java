package minijava.comun.config;

// Un interruptor por logro que cambia el comportamiento observable, para poder
// demostrar también el comportamiento base. Los dos primeros los lee el AF.
public record ConfiguracionDelCompilador(
        boolean floatsHabilitados,
        boolean unicodeHabilitado,
        boolean reporteElegante,
        boolean multiDeteccionDeErrores) {

    public static final ConfiguracionDelCompilador DEFAULT =
            new ConfiguracionDelCompilador(true, true, true, true);
}
