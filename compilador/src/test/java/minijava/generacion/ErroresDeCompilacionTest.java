package minijava.generacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los errores que agrega la Etapa 5, los del punto de entrada (CONVENCIONES-CATEDRA
// §1), y los de la Etapa 4 corridos por la Etapa 5: el reporte es el de
// siempre, mensaje, línea en blanco y código (Pautas Etapa 5, slide 26), y no se
// escribe el archivo de salida.
@Tag("generacion")
class ErroresDeCompilacionTest {

    static Stream<Path> casosDeLaEtapa() throws IOException {
        return casosDe(Path.of("resources", "conErrores"));
    }

    static Stream<Path> casosConErroresDeLaEtapaCuatro() throws IOException {
        return casosDe(Path.of("resources", "sentencias", "conErrores"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosDeLaEtapa")
    void unErrorDeLaEtapaDaSuCodigoYNoGeneraCodigo(Path caso) throws IOException {
        controlar(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConErroresDeLaEtapaCuatro")
    void unErrorDeLaEtapaCuatroSigueDandoElMismoCodigo(Path caso) throws IOException {
        controlar(caso);
    }

    private static void controlar(Path caso) throws IOException {
        String esperado = Files.readAllLines(caso, StandardCharsets.UTF_8).getFirst().substring(3);
        Compilacion compilacion = Compilacion.de(caso);
        assertEquals(3, compilacion.reporte().size(), caso + ": " + compilacion.reporte());
        assertEquals("", compilacion.reporte().get(1));
        assertEquals(esperado, compilacion.reporte().get(2), caso.toString());
        String linea = esperado.substring(esperado.lastIndexOf('|') + 1, esperado.length() - 1);
        assertTrue(compilacion.reporte().getFirst().contains("linea " + linea), compilacion.reporte().getFirst());
        assertNull(compilacion.codigo(), caso + ": con un error no se escribe la salida");
    }

    private static Stream<Path> casosDe(Path directorio) throws IOException {
        try (Stream<Path> archivos = Files.list(directorio)) {
            return archivos.filter(Files::isRegularFile).sorted().toList().stream();
        }
    }
}
