package minijava.comun.config;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

// Qué logros están prendidos. El compilador tiene que poder correrse con 0, 1 o
// N logros en cualquier combinación, y la de cero debe reproducir exactamente la
// especificación base de la cátedra: por eso la configuración es un conjunto y
// no una lista de booleanos posicionales.
public final class ConfiguracionDelCompilador {

    public static final ConfiguracionDelCompilador DEFAULT = todos();

    private final Set<Logro> logros;

    private ConfiguracionDelCompilador(EnumSet<Logro> logros) {
        this.logros = Collections.unmodifiableSet(logros);
    }

    public static ConfiguracionDelCompilador ninguno() {
        return new ConfiguracionDelCompilador(EnumSet.noneOf(Logro.class));
    }

    public static ConfiguracionDelCompilador todos() {
        return new ConfiguracionDelCompilador(EnumSet.allOf(Logro.class));
    }

    public static ConfiguracionDelCompilador soloEtapa(int etapa) {
        EnumSet<Logro> conjunto = EnumSet.noneOf(Logro.class);
        for (Logro logro : Logro.values()) {
            if (logro.getEtapa() == etapa) {
                conjunto.add(logro);
            }
        }
        return new ConfiguracionDelCompilador(conjunto);
    }

    public static ConfiguracionDelCompilador de(Set<Logro> logros) {
        return new ConfiguracionDelCompilador(logros.isEmpty()
                ? EnumSet.noneOf(Logro.class)
                : EnumSet.copyOf(logros));
    }

    public ConfiguracionDelCompilador con(Logro... agregados) {
        EnumSet<Logro> conjunto = copia();
        Collections.addAll(conjunto, agregados);
        return new ConfiguracionDelCompilador(conjunto);
    }

    public ConfiguracionDelCompilador sin(Logro... quitados) {
        EnumSet<Logro> conjunto = copia();
        for (Logro logro : quitados) {
            conjunto.remove(logro);
        }
        return new ConfiguracionDelCompilador(conjunto);
    }

    public boolean tiene(Logro logro) {
        return logros.contains(logro);
    }

    public Set<Logro> getLogros() {
        return logros;
    }

    public boolean floatsHabilitados() {
        return tiene(Logro.FLOATS);
    }

    public boolean unicodeHabilitado() {
        return tiene(Logro.UNICODES);
    }

    public boolean reporteElegante() {
        return tiene(Logro.REPORTE_ELEGANTE);
    }

    public boolean multiDeteccionDeErrores() {
        return tiene(Logro.MULTI_DETECCION);
    }

    private EnumSet<Logro> copia() {
        return logros.isEmpty() ? EnumSet.noneOf(Logro.class) : EnumSet.copyOf(logros);
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof ConfiguracionDelCompilador otra && logros.equals(otra.logros);
    }

    @Override
    public int hashCode() {
        return logros.hashCode();
    }

    @Override
    public String toString() {
        return "ConfiguracionDelCompilador" + logros;
    }
}
