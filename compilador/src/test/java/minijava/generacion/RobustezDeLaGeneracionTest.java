package minijava.generacion;

import minijava.casos.CorpusEmpaquetado;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

// Todos los fuentes de las etapas anteriores, unos 40.000. A cada uno se le agrega
// una clase con un main vacío, para que la generación corra aunque el fuente no
// tenga punto de entrada. El compilador nunca se cae: termina en un código de la
// cátedra, y si fue [SinErrores], el código que escribió ensambla en la CeIVM.
@Tag("generacion")
class RobustezDeLaGeneracionTest {

    private static final String MAIN = "\nclass ZzRobustez { static void main() { } }\n";

    static Stream<Path> corpus() throws IOException {
        return Stream.of(Path.of("resources", "lexico"), Path.of("resources", "sintactico"),
                Path.of("resources", "declaraciones"), Path.of("resources", "sentencias"),
                CorpusEmpaquetado.extraido(CorpusEmpaquetado.SENTENCIAS_GENERADO),
                CorpusEmpaquetado.extraido(CorpusEmpaquetado.SINTACTICO_GENERADO),
                CorpusEmpaquetado.extraido(CorpusEmpaquetado.CORPUS_EXTERNO));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void laGeneracionNoSeCaeYLoGeneradoEnsambla(Path corpus) throws IOException {
        List<String> fallas = new ArrayList<>();
        int generados = 0;
        for (Path fuente : fuentesDe(corpus)) {
            String texto = Files.readString(fuente, StandardCharsets.ISO_8859_1);
            try {
                Compilacion compilacion = Compilacion.deTexto(texto + MAIN);
                String ultima = compilacion.reporte().isEmpty() ? "" : compilacion.reporte().getLast();
                if (!ultima.equals("[SinErrores]") && !ultima.startsWith("[Error:")) {
                    fallas.add(fuente + " -> " + compilacion.reporte());
                } else if (compilacion.exitosa()) {
                    generados++;
                    String error = EjecutorCeIVM.errorDeEnsamblado(compilacion.codigo());
                    if (error != null) {
                        fallas.add(fuente + " no ensambla: " + error);
                    }
                }
            } catch (RuntimeException excepcion) {
                fallas.add(fuente + " -> " + excepcion);
            }
        }
        assertEquals(List.of(), fallas.stream().limit(5).toList(), fallas.size() + " fallas");
        assertTrue(generados > 0 || !corpus.toString().contains("sentencias"), corpus + ": nada llego a generarse");
    }

    private static List<Path> fuentesDe(Path raiz) throws IOException {
        try (Stream<Path> archivos = Files.walk(raiz)) {
            return archivos.filter(Files::isRegularFile).sorted().toList();
        }
    }
}
