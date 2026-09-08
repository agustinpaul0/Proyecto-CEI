package minijava.aplicacion;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.ParserDeConfiguracion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Un solo jar sirve a las dos etapas, porque la Etapa 1 se corrige por separado
// y tiene que seguir siendo ejecutable sin recompilar nada.
//
// El contrato de invocación de las Pautas (Etapa 2, slide 28) es
// `java -jar Compilador.jar programa1.java`, con cualquier extensión: por eso el
// único argumento obligatorio es la ruta, y las opciones llevan `--`.
public record OpcionesDeInvocacion(
        int etapa,
        ConfiguracionDelCompilador configuracion,
        Optional<String> rutaFuente,
        List<String> avisos) {

    public static final int ETAPA_POR_DEFECTO = 2;

    private static final String OPCION_ETAPA = "--etapa=";
    private static final String OPCION_LOGROS = "--logros=";

    public static OpcionesDeInvocacion parsear(String[] argumentos) {
        List<String> avisos = new ArrayList<>();
        int etapa = ETAPA_POR_DEFECTO;
        String logros = null;
        String ruta = null;

        for (String argumento : argumentos) {
            if (argumento.startsWith(OPCION_ETAPA)) {
                etapa = etapaDe(argumento.substring(OPCION_ETAPA.length()), etapa, avisos);
            } else if (argumento.startsWith(OPCION_LOGROS)) {
                logros = argumento.substring(OPCION_LOGROS.length());
            } else if (argumento.startsWith("--")) {
                avisos.add("Opcion desconocida, se ignora: " + argumento);
            } else if (ruta == null) {
                ruta = argumento;
            } else {
                avisos.add("Se ignora el argumento de mas: " + argumento);
            }
        }

        // Sin --logros explícito, la etapa elegida manda: correr la Etapa 1
        // reproduce la entrega de la Etapa 1, con sus logros y sin los tokens
        // que la Etapa 2 agrega. Correr la Etapa 2 los prende todos.
        var resultado = logros != null
                ? ParserDeConfiguracion.parsear(logros)
                : new ParserDeConfiguracion.Resultado(configuracionPorDefectoDe(etapa), List.of());
        avisos.addAll(resultado.avisos());

        return new OpcionesDeInvocacion(etapa, resultado.configuracion(),
                Optional.ofNullable(ruta), List.copyOf(avisos));
    }

    private static ConfiguracionDelCompilador configuracionPorDefectoDe(int etapa) {
        return etapa == 1
                ? ConfiguracionDelCompilador.soloEtapa(1)
                : ConfiguracionDelCompilador.todos();
    }

    private static int etapaDe(String valor, int actual, List<String> avisos) {
        try {
            int etapa = Integer.parseInt(valor.trim());
            if (etapa == 1 || etapa == 2) {
                return etapa;
            }
        } catch (NumberFormatException excepcion) {
            // Cae en el aviso de abajo, igual que un número fuera de rango.
        }
        avisos.add("Etapa no soportada, se usa la " + actual + ": " + valor);
        return actual;
    }
}
