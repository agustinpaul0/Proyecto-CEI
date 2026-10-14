package minijava.sentencias;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// El corpus de la Etapa 3 corrido con la Etapa 4. El chequeo de sentencias corre
// sólo si las declaraciones están bien (Pautas Etapa 4, slide 22), así que cada
// caso con error da el mismo código. Los correctos siguen correctos, salvo el que
// tiene un cuerpo mal escrito a propósito para mostrar que la Etapa 3 no lo mira.
@Tag("sentencias")
class ConsistenciaEntreEtapasTest {

    private static final Path DECLARACIONES = Path.of("resources", "declaraciones");
    private static final String CUERPO_MAL_ESCRITO = "cuerposNoSeChequean.java";

    static Stream<Path> correctos() throws IOException {
        return casos("sinErrores").filter(caso -> !caso.endsWith(CUERPO_MAL_ESCRITO));
    }

    static Stream<Path> conErrores() throws IOException {
        return casos("conErrores");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("correctos")
    void unCorrectoDeLaEtapaTresSigueCorrecto(Path caso) throws IOException {
        assertEquals("[SinErrores]", ejecutar(caso).getLast());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("conErrores")
    void unErrorDeDeclaracionTieneElMismoCodigo(Path caso) throws IOException {
        String esperado = Files.readAllLines(caso).getFirst().replaceFirst("^/+", "");
        assertEquals(esperado, ejecutar(caso).getLast());
    }

    @Test
    void elCuerpoMalEscritoSeDetectaEnLaEtapaCuatro() throws IOException {
        Path caso = DECLARACIONES.resolve("sinErrores").resolve(CUERPO_MAL_ESCRITO);
        assertEquals("[Error:.|4]", ejecutar(caso).getLast());
    }

    private static Stream<Path> casos(String carpeta) throws IOException {
        try (Stream<Path> archivos = Files.list(DECLARACIONES.resolve(carpeta))) {
            return archivos.sorted().toList().stream();
        }
    }

    private List<String> ejecutar(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(caso.toString());
        return salida.getLineas();
    }
}
