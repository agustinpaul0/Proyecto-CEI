package minijava.logros;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.ParserDeConfiguracion;

// Los logros con los que corre ESTA ejecución de la suite, tomados de -Dlogros.
//
// Se resuelve una vez por corrida: -Dlogros no cambia en el medio, y releerlo en
// cada test haría que dos tests de la misma corrida pudieran ver cosas distintas.
public final class ConfiguracionDeLaCorrida {

    private static final ConfiguracionDelCompilador ACTUAL =
            ParserDeConfiguracion.desdePropiedadDeSistema().configuracion();

    private ConfiguracionDeLaCorrida() {
    }

    public static ConfiguracionDelCompilador actual() {
        return ACTUAL;
    }

    // La misma configuración, escrita como la espera --logros. Para los testers
    // que ejercitan el compilador invocando Main y no construyéndolo a mano.
    public static String comoArgumentoDeLinea() {
        if (ACTUAL.getLogros().isEmpty()) {
            return "--logros=ninguno";
        }
        return "--logros=" + ACTUAL.getLogros().stream()
                .map(minijava.comun.config.Logro::getNombre)
                .collect(java.util.stream.Collectors.joining(","));
    }
}
