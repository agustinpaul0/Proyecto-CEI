package minijava.generacion;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// El código de los métodos de Object y System, que no vienen del fuente (Apunte
// §14). Son unidades como cualquier otra: el prólogo y el epílogo los pone
// Unidad; acá va sólo el cuerpo. Los estáticos de un parámetro lo tienen en 3, y
// el resultado de read en 3; toString, de instancia, devuelve en 4.
public final class Predefinidos {

    private static final List<String> SIN_CUERPO = List.of();

    // Apunte §14: printSln controla con DEREF que la referencia no sea nula, y
    // SPRINT recibe la dirección del primer carácter, en el desplazamiento 2.
    private static final List<String> IMPRIMIR_STRING =
            List.of("LOAD 3", "DUP", "DEREF", "POP", "PUSH 2", "ADD", "SPRINT");

    private static final Map<String, List<String>> CUERPOS = Map.ofEntries(
            // Documento §7: debugPrint imprime un entero y finaliza la línea.
            Map.entry("Object.debugPrint/1", List.of("LOAD 3", "IPRINT", "PRNLN")),
            // Documento §7: read lee el próximo byte; la CeIVM devuelve su código.
            Map.entry("System.read/0", List.of("READ", "STORE 3")),
            Map.entry("System.printB/1", List.of("LOAD 3", "BPRINT")),
            Map.entry("System.printC/1", List.of("LOAD 3", "CPRINT")),
            Map.entry("System.printI/1", List.of("LOAD 3", "IPRINT")),
            Map.entry("System.printS/1", IMPRIMIR_STRING),
            Map.entry("System.println/0", List.of("PRNLN")),
            Map.entry("System.printBln/1", List.of("LOAD 3", "BPRINT", "PRNLN")),
            Map.entry("System.printCln/1", List.of("LOAD 3", "CPRINT", "PRNLN")),
            Map.entry("System.printIln/1", List.of("LOAD 3", "IPRINT", "PRNLN")),
            Map.entry("System.printSln/1", concatenar(IMPRIMIR_STRING, "PRNLN")));

    private Predefinidos() {
    }

    // Una unidad sin cuerpo es un constructor por defecto o un método predefinido.
    public static void generarCuerpo(Generador g, String clase, String nombre, int aridad) {
        String clave = clase + "." + nombre + "/" + aridad;
        if (clave.equals("Object.toString/0")) {
            // Apunte §14: retorna el String vacío.
            RutinasDeSoporte.alojarString(g, 0);
            g.gen("STORE 4");
            return;
        }
        CUERPOS.getOrDefault(clave, SIN_CUERPO).forEach(g::gen);
    }

    private static List<String> concatenar(List<String> instrucciones, String ultima) {
        return Stream.concat(instrucciones.stream(), Stream.of(ultima)).toList();
    }
}
