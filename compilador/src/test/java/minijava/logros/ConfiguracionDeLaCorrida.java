package minijava.logros;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.ParserDeConfiguracion;

// Los logros con los que corre ESTA ejecución de la suite, tomados de -Dlogros.
// Es lo que permite correr la misma suite en varias configuraciones
// (`mvn test -Dlogros=ninguno`, `-Dlogros=todos`, `-Dlogros=etapa1,ternario`) y
// que cada test se saltee solo cuando su logro no corresponde.
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
}
