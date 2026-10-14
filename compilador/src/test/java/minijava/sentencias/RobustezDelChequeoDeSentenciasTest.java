package minijava.sentencias;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.casos.CorpusEmpaquetado;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El parser arma los AST también cuando el Modo Pánico corta una sentencia por la
// mitad, y el chequeo de sentencias recorre cuerpos de todas las formas que la
// gramática acepta, incluidas las de los logros que no se controlan. Sobre todos
// los fuentes de las etapas anteriores, el compilador de la Etapa 4 nunca puede
// caerse y siempre termina en un código de la cátedra.
@Tag("sentencias")
class RobustezDelChequeoDeSentenciasTest {

    static Stream<Path> corpus() throws IOException {
        return Stream.of(Path.of("resources", "lexico"), Path.of("resources", "sintactico"),
                Path.of("resources", "declaraciones"),
                CorpusEmpaquetado.extraido(CorpusEmpaquetado.SINTACTICO_GENERADO),
                CorpusEmpaquetado.extraido(CorpusEmpaquetado.CORPUS_EXTERNO));
    }

    @ParameterizedTest
    @MethodSource("corpus")
    void elCompiladorTerminaSiempreEnUnCodigo(Path corpus) throws IOException {
        List<String> fallas = new ArrayList<>();
        for (Path fuente : fuentesDe(corpus)) {
            List<String> salida = ejecutar(fuente, fallas);
            if (salida != null && !terminaEnUnCodigo(salida)) {
                fallas.add(fuente + " -> " + salida);
            }
        }
        assertEquals(List.of(), fallas);
    }

    // Un fuente sintácticamente válido da un único reporte: el éxito o el primer error.
    @Test
    void unFuenteSintacticamenteValidoDaUnSoloReporte() throws IOException {
        List<String> fallas = new ArrayList<>();
        List<Path> fuentes = Stream.concat(
                fuentesDe(Path.of("resources", "sintactico", "sinErrores")).stream(),
                fuentesDe(CorpusEmpaquetado.extraido(CorpusEmpaquetado.SINTACTICO_GENERADO)
                        .resolve("sinErrores")).stream()).toList();
        for (Path fuente : fuentes) {
            List<String> salida = ejecutar(fuente, fallas);
            if (salida != null && salida.size() != 3) {
                fallas.add(fuente + " -> " + salida);
            }
        }
        assertTrue(fuentes.size() > 1000, "el corpus sintactico no esta donde se lo busca");
        assertEquals(List.of(), fallas);
    }

    private List<String> ejecutar(Path fuente, List<String> fallas) {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        try {
            ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(fuente.toString());
            return salida.getLineas();
        } catch (IOException | RuntimeException excepcion) {
            fallas.add(fuente + " -> " + excepcion);
            return null;
        }
    }

    private boolean terminaEnUnCodigo(List<String> salida) {
        String ultima = salida.getLast();
        return ultima.equals("[SinErrores]") || ultima.startsWith("[Error:");
    }

    private List<Path> fuentesDe(Path raiz) throws IOException {
        try (Stream<Path> archivos = Files.walk(raiz)) {
            return archivos.filter(Files::isRegularFile)
                    .filter(archivo -> archivo.toString().endsWith(".java"))
                    .sorted().toList();
        }
    }
}
