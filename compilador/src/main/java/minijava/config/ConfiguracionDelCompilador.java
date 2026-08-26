package minijava.config;

public record ConfiguracionDelCompilador(
        boolean floatsHabilitados,
        boolean unicodeHabilitado,
        boolean reporteElegante,
        boolean multiDeteccionDeErrores) {

    public static final ConfiguracionDelCompilador DEFAULT =
            new ConfiguracionDelCompilador(true, true, true, true);
}
