package minijava.generacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los errores de ejecución (Apunte §17), que la guía de los testers pide probar a
// mano. La primera línea lista lo que el programa imprime antes de fallar y, al
// final, el error: el mensaje de una rutina de soporte, que termina con HALT, o el
// de la propia CeIVM (referencia nula, división por cero). Después del error no se
// imprime nada más.
@Tag("generacion")
class ErroresDeEjecucionTest {

    static Stream<Path> casos() throws IOException {
        try (Stream<Path> archivos = Files.list(Path.of("resources", "errores-de-ejecucion"))) {
            return archivos.filter(Files::isRegularFile).sorted().toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void imprimeHastaElErrorYTermina(Path caso) throws IOException {
        List<String> esperados = new ArrayList<>(List.of(
                Files.readAllLines(caso, StandardCharsets.UTF_8).getFirst().substring(3).split("&")));
        String error = esperados.removeLast();
        EjecutorCeIVM.Ejecucion ejecucion = EjecucionDelCorpusTest.correrEnLaCeIVM(caso);
        String impreso = ejecucion.salida().strip();
        if (ejecucion.terminoBien()) {
            assertTrue(impreso.endsWith(error), caso + ": el mensaje del error es lo ultimo impreso\n" + impreso);
            impreso = impreso.substring(0, impreso.length() - error.length()).strip();
        } else {
            assertNotNull(ejecucion.error());
            assertTrue(ejecucion.error().contains(error), caso + ": " + ejecucion.error());
        }
        assertEquals(String.join("\n", esperados), impreso, caso + ": lo impreso antes del error");
    }
}
