package minijava.aplicacion;

import java.util.ArrayList;
import java.util.List;

// Un solo jar sirve a todas las etapas, porque cada una se corrige por separado
// y tiene que seguir siendo ejecutable sin recompilar nada.
//
// El contrato de invocación de las Pautas es `java -jar Compilador.jar
// programa1.java` (Etapa 2, slide 28) y, desde la Etapa 5, `java -jar
// Compilador.jar programa1.java salida.txt` (slide 30), con cualquier extensión:
// por eso los argumentos son posicionales y las opciones llevan `--`. Lo que no
// se pasó es null.
public record OpcionesDeInvocacion(
        int etapa,
        String rutaFuente,
        String rutaSalida,
        List<String> avisos) {

    public static final int ETAPA_POR_DEFECTO = 5;

    private static final String OPCION_ETAPA = "--etapa=";

    public static OpcionesDeInvocacion parsear(String[] argumentos) {
        List<String> avisos = new ArrayList<>();
        int etapa = ETAPA_POR_DEFECTO;
        String ruta = null;
        String salida = null;

        for (String argumento : argumentos) {
            if (argumento.startsWith(OPCION_ETAPA)) {
                etapa = etapaDe(argumento.substring(OPCION_ETAPA.length()), etapa, avisos);
            } else if (argumento.startsWith("--")) {
                avisos.add("Opcion desconocida, se ignora: " + argumento);
            } else if (ruta == null) {
                ruta = argumento;
            } else if (salida == null) {
                salida = argumento;
            } else {
                avisos.add("Se ignora el argumento de mas: " + argumento);
            }
        }

        return new OpcionesDeInvocacion(etapa, ruta, salida, List.copyOf(avisos));
    }

    private static int etapaDe(String valor, int actual, List<String> avisos) {
        try {
            int etapa = Integer.parseInt(valor.trim());
            if (etapa >= 1 && etapa <= ETAPA_POR_DEFECTO) {
                return etapa;
            }
        } catch (NumberFormatException excepcion) {
            // Cae en el aviso de abajo, igual que un número fuera de rango.
        }
        avisos.add("Etapa no soportada, se usa la " + actual + ": " + valor);
        return actual;
    }
}
