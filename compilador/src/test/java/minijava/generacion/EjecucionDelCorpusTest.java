package minijava.generacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El corpus de la Etapa 5, con dos oráculos que no son el compilador:
// - la primera línea de cada caso, `///a&b&exitosamente`, como en el tester de la
//   cátedra, pero en orden: cada fragmento aparece después del anterior;
// - Java: el mismo fuente, corrido con javac y la JVM, imprime exactamente lo mismo.
//   Un caso cuya segunda línea es `// sin-contraste: <motivo>` se saltea, porque
//   depende de algo en que MiniJava difiere de Java a propósito.
@Tag("generacion")
class EjecucionDelCorpusTest {

    static final Path CORPUS = Path.of("resources", "sinErrores");
    private static final String SIN_CONTRASTE = "// sin-contraste";

    static Stream<Path> casos() throws IOException {
        try (Stream<Path> archivos = Files.list(CORPUS)) {
            return archivos.filter(Files::isRegularFile).sorted().toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void imprimeLoQueDiceSuPrimeraLinea(Path caso) {
        EjecutorCeIVM.Ejecucion ejecucion = correrEnLaCeIVM(caso);
        assertTrue(ejecucion.terminoBien(), caso + ": " + ejecucion.error() + "\n" + ejecucion.salida());
        List<String> esperados = new ArrayList<>(List.of(lineas(caso).getFirst().substring(3).split("&")));
        assertEquals("exitosamente", esperados.removeLast(), caso + ": la primera linea termina en &exitosamente");
        int desde = 0;
        for (String esperado : esperados) {
            int encontrado = ejecucion.salida().indexOf(esperado, desde);
            assertTrue(encontrado >= 0, caso + ": falta «" + esperado + "» despues de la posicion " + desde
                    + " en\n" + ejecucion.salida());
            desde = encontrado + esperado.length();
        }
    }

    @Test
    void imprimeLoMismoQueJava() throws IOException {
        Map<String, String> programas = new LinkedHashMap<>();
        for (Path caso : casos().toList()) {
            List<String> lineas = lineas(caso);
            if (lineas.size() < 2 || !lineas.get(1).startsWith(SIN_CONTRASTE)) {
                programas.put(caso.toString(), Files.readString(caso, StandardCharsets.UTF_8));
            }
        }
        Map<String, String> enJava = EjecutorJava.correr(programas);
        List<String> diferencias = new ArrayList<>();
        for (String caso : programas.keySet()) {
            String enLaCeIVM = correrEnLaCeIVM(Path.of(caso)).salida();
            if (!enLaCeIVM.equals(enJava.get(caso))) {
                diferencias.add(caso + "\n--- CeIVM:\n" + enLaCeIVM + "\n--- Java:\n" + enJava.get(caso));
            }
        }
        assertEquals(List.of(), diferencias);
    }

    static EjecutorCeIVM.Ejecucion correrEnLaCeIVM(Path caso) {
        Compilacion compilacion = Compilacion.de(caso);
        assertTrue(compilacion.exitosa(), caso + ": " + compilacion.reporte());
        return EjecutorCeIVM.correr(compilacion.codigo());
    }

    private static List<String> lineas(Path caso) {
        try {
            return Files.readAllLines(caso, StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
