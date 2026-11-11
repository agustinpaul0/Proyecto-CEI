package minijava.generacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El contraste diferencial: programas generados al azar, con semillas fijas, que
// tienen que compilar, correr bien en la CeIVM e imprimir exactamente lo mismo que
// en Java. Por defecto son 300; con -Ddiferencial.programas=3000, más.
@Tag("generacion")
class ContrasteDiferencialTest {

    private static final int POR_DEFECTO = 300;
    private static final int TANDA = 100;

    @Test
    void cadaProgramaGeneradoImprimeLoMismoQueEnJava() {
        int cantidad = Integer.getInteger("diferencial.programas", POR_DEFECTO);
        List<String> diferencias = new ArrayList<>();
        for (int desde = 0; desde < cantidad; desde += TANDA) {
            Map<String, String> programas = new LinkedHashMap<>();
            for (int semilla = desde; semilla < Math.min(cantidad, desde + TANDA); semilla++) {
                programas.put("semilla " + semilla, new GeneradorDeProgramas(semilla).programa());
            }
            Map<String, String> enJava = EjecutorJava.correr(programas);
            programas.forEach((nombre, fuente) -> {
                EjecutorCeIVM.Ejecucion ejecucion = Compilacion.ejecutar(fuente);
                if (!ejecucion.terminoBien() || !ejecucion.salida().equals(enJava.get(nombre))) {
                    diferencias.add(nombre + " (" + ejecucion.error() + ")\n" + fuente
                            + "\n--- CeIVM:\n" + ejecucion.salida() + "\n--- Java:\n" + enJava.get(nombre));
                }
            });
        }
        assertEquals(List.of(), diferencias.stream().limit(3).toList(), diferencias.size() + " diferencias");
        assertTrue(cantidad > 0);
    }
}
