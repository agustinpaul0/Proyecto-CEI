package minijava.lexico;

import minijava.aplicacion.ModuloPrincipal;
import minijava.casos.VolcadoDeCasos;
import minijava.salida.SalidaEnMemoria;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reprocesa todo resources/regresion/, donde el fuzzer persiste append-only lo que
// hizo fallar una corrida. No hay oráculo por caso —son entradas aleatorias—, así
// que se verifica el contrato general: termina, no crashea, y respeta el formato.
class CorpusDeRegresionTest {

    private static final Path DIRECTORIO = Path.of("resources", "regresion");

    static Stream<Path> casos() throws IOException {
        if (!Files.isDirectory(DIRECTORIO)) {
            return Stream.empty();
        }
        try (var archivos = Files.list(DIRECTORIO)) {
            return archivos.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted(Comparator.naturalOrder())
                    .toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void elCasoPersistidoSigueSiendoProcesableSinFallosNoControlados(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(caso.toString());

        List<String> lineas = salida.getLineas();
        VolcadoDeCasos.volcar(caso.toString(), lineas);
        assertFalse(lineas.isEmpty(), () -> caso + ": no produjo ninguna salida");
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("(EOF,")),
                () -> caso + ": el analisis no llego al token EOF.\n" + String.join("\n", lineas));
        assertTrue(lineas.stream().noneMatch(l -> l.indexOf('\n') >= 0 || l.indexOf('\r') >= 0),
                () -> caso + ": una linea de salida lleva un salto de linea embebido.\n"
                        + String.join("\n", lineas));
        assertTrue(lineas.stream().filter(l -> l.startsWith("[Error:"))
                        .noneMatch(l -> l.chars().anyMatch(c -> c < 0x20)),
                () -> caso + ": un codigo de error lleva un caracter de control crudo.\n"
                        + String.join("\n", lineas));
    }

    @AfterAll
    static void informarElCorpusProcesado() {
        VolcadoDeCasos.resumirDirectorio(DIRECTORIO.toString());
    }

    // Si alguien borra el directorio, el test de arriba pasaría vacío y en silencio.
    @Test
    void elDirectorioDeRegresionExisteYNoEstaVacio() throws IOException {
        assertTrue(Files.isDirectory(DIRECTORIO), DIRECTORIO + " no existe");
        assertTrue(casos().findAny().isPresent(),
                DIRECTORIO + " esta vacio: las regresiones son append-only, no se borran");
    }
}
