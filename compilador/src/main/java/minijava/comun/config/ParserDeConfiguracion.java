package minijava.comun.config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// Un nombre desconocido NO aborta: se junta como aviso y se sigue con el resto.
// El compilador tiene que arrancar igual, porque los testers de la cátedra
// invocan main() in-process y una excepción acá mataría la corrida entera.
public final class ParserDeConfiguracion {

    public static final String PROPIEDAD_DE_SISTEMA = "logros";

    private static final String NINGUNO = "ninguno";
    private static final String TODOS = "todos";
    private static final String PREFIJO_DE_ETAPA = "etapa";

    public record Resultado(ConfiguracionDelCompilador configuracion, List<String> avisos) {
    }

    private ParserDeConfiguracion() {
    }

    // Sin especificación (null o vacía) se prenden todos: es el modo en el que se
    // entrega, y el que demuestra los logros.
    public static Resultado parsear(String especificacion) {
        if (especificacion == null || especificacion.isBlank()) {
            return new Resultado(ConfiguracionDelCompilador.todos(), List.of());
        }

        List<String> avisos = new ArrayList<>();
        EnumSet<Logro> acumulados = EnumSet.noneOf(Logro.class);

        for (String parte : especificacion.split(",")) {
            String nombre = parte.trim().toLowerCase();
            if (nombre.isEmpty()) {
                continue;
            }
            if (nombre.equals(NINGUNO)) {
                acumulados.clear();
                continue;
            }
            if (nombre.equals(TODOS)) {
                acumulados.addAll(EnumSet.allOf(Logro.class));
                continue;
            }
            if (nombre.startsWith(PREFIJO_DE_ETAPA)) {
                agregarEtapa(nombre, acumulados, avisos);
                continue;
            }
            Logro.porNombre(nombre).ifPresentOrElse(
                    acumulados::add,
                    () -> avisos.add("Logro desconocido, se ignora: " + nombre));
        }

        return new Resultado(ConfiguracionDelCompilador.de(acumulados), List.copyOf(avisos));
    }

    public static Resultado desdePropiedadDeSistema() {
        return parsear(System.getProperty(PROPIEDAD_DE_SISTEMA));
    }

    private static void agregarEtapa(String nombre, EnumSet<Logro> acumulados, List<String> avisos) {
        String numero = nombre.substring(PREFIJO_DE_ETAPA.length());
        int etapa;
        try {
            etapa = Integer.parseInt(numero);
        } catch (NumberFormatException excepcion) {
            avisos.add("Etapa no reconocida, se ignora: " + nombre);
            return;
        }
        var deLaEtapa = ConfiguracionDelCompilador.soloEtapa(etapa).getLogros();
        if (deLaEtapa.isEmpty()) {
            avisos.add("No hay logros para la etapa " + etapa + ", se ignora: " + nombre);
            return;
        }
        acumulados.addAll(deLaEtapa);
    }
}
