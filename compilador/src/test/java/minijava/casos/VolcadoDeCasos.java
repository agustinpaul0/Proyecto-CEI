package minijava.casos;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Volcado a consola de lo que produjo cada caso del corpus, para poder mirar el
// reporte —tokens, [Error:...] y las cuatro líneas del reporte elegante— sin
// tener que romper un test a propósito. Lo comparten los cuatro runners de corpus.
//
// Se controla con -Dvolcado:
//   completo (default)  encabezado + salida entera del compilador + pie con los conteos
//   resumen             una línea por caso y sólo sus códigos [Error:...]
//   off                 nada
//
// Vuelve a prender lo que 484d0fc había apagado (el flag de los testers de la
// cátedra), pero ahora apagable desde la línea de comandos y en los cuatro runners.
public final class VolcadoDeCasos {

    private static final String MODO = normalizar(System.getProperty("volcado", "completo"));

    // Casos procesados y casos con al menos un error, por directorio. Lo lleva acá
    // y no cada runner para que el pie salga aunque el runner no cuente nada.
    private static final Map<String, int[]> TALLY = new ConcurrentHashMap<>();

    private VolcadoDeCasos() {
    }

    // Para quien tenga que hacer trabajo extra —volver a correr el módulo
    // principal, por ejemplo— sólo si se va a mirar el resultado.
    public static boolean habilitado() {
        return !MODO.equals("off");
    }

    private static String normalizar(String valor) {
        String limpio = valor == null ? "completo" : valor.strip().toLowerCase();
        return switch (limpio) {
            case "off", "false", "no", "none" -> "off";
            case "resumen", "summary" -> "resumen";
            default -> "completo";
        };
    }

    public static void volcar(String caso, List<String> salida) {
        volcar(System.out, caso, salida);
    }

    // Los testers de la cátedra redirigen System.out para capturar al compilador:
    // tienen que pasar el stream original o el volcado se lo comen ellos mismos.
    public static void volcar(PrintStream consola, String caso, String salidaCruda) {
        volcar(consola, caso, salidaCruda.isEmpty() ? List.of() : List.of(salidaCruda.split("\\R", -1)));
    }

    public static void volcar(PrintStream consola, String caso, List<String> salida) {
        int errores = contar(salida, true);
        int tokens = contar(salida, false);
        registrar(caso, errores > 0);

        switch (MODO) {
            case "off" -> { }
            case "resumen" -> {
                consola.printf("  %-58s %3d tok  %2d err%n", nombre(caso), tokens, errores);
                salida.stream().filter(VolcadoDeCasos::esError)
                        .forEach(linea -> consola.println("      " + linea));
            }
            default -> {
                consola.println("----- " + caso);
                salida.forEach(consola::println);
                // El conteo de tokens es del corpus léxico; en el sintáctico no
                // hay ninguno y el pie se queda sólo con los errores.
                consola.println("----- " + plural(errores, "error", "errores")
                        + (tokens == 0 ? "" : ", " + plural(tokens, "token", "tokens")));
            }
        }
        consola.flush();
    }

    // Pie de suite: es lo que responde "qué archivos se detectaron".
    public static void resumirDirectorio(String directorio) {
        resumirDirectorio(System.out, directorio);
    }

    public static void resumirDirectorio(PrintStream consola, String directorio) {
        int[] cuenta = TALLY.remove(clave(directorio));
        if (MODO.equals("off") || cuenta == null) {
            return;
        }
        consola.println("===== " + clave(directorio) + ": "
                + plural(cuenta[0], "caso procesado", "casos procesados")
                + ", " + cuenta[1] + " con errores");
        consola.flush();
    }

    private static void registrar(String caso, boolean conErrores) {
        int[] cuenta = TALLY.computeIfAbsent(directorioDe(caso), ignorado -> new int[2]);
        synchronized (cuenta) {
            cuenta[0]++;
            if (conErrores) {
                cuenta[1]++;
            }
        }
    }

    private static int contar(List<String> salida, boolean errores) {
        return (int) salida.stream().filter(linea -> errores ? esError(linea) : esToken(linea)).count();
    }

    private static boolean esError(String linea) {
        return linea.startsWith("[Error:");
    }

    private static boolean esToken(String linea) {
        return linea.startsWith("(") && linea.endsWith(")");
    }

    private static String plural(int cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    private static String nombre(String caso) {
        return Path.of(caso).getFileName().toString();
    }

    private static String directorioDe(String caso) {
        Path padre = Path.of(caso).getParent();
        return padre == null ? "." : clave(padre.toString());
    }

    private static String clave(String directorio) {
        return directorio.replace('\\', '/').replaceAll("/+$", "");
    }
}
