package minijava.sentencias;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.casos.CorpusEmpaquetado;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El corpus combinatorio de herramientas/sentencias/generar_corpus_sentencias.py:
// la conformidad, los operadores, las condiciones, los índices y los tamaños,
// celda por celda, cada una contrastada con javac al generarla. Cada caso tiene
// que dar exactamente un reporte con su código.
@Tag("sentencias")
class CorpusGeneradoSentenciasTest {

    @ParameterizedTest
    @ValueSource(strings = {"sinErrores", "conErrores"})
    void cadaCasoDaExactamenteSuCodigo(String carpeta) throws IOException {
        Path raiz = CorpusEmpaquetado.extraido(CorpusEmpaquetado.SENTENCIAS_GENERADO).resolve(carpeta);
        List<Path> casos;
        try (Stream<Path> archivos = Files.list(raiz)) {
            casos = archivos.sorted().toList();
        }
        List<String> fallas = new ArrayList<>();
        for (Path caso : casos) {
            String esperado = carpeta.equals("sinErrores") ? "[SinErrores]"
                    : Files.readAllLines(caso).getFirst().replaceFirst("^/+", "");
            List<String> salida = ejecutar(caso);
            if (salida.size() != 3 || !salida.getLast().equals(esperado)) {
                fallas.add(caso.getFileName() + ": esperado " + esperado + ", salida " + salida);
            }
        }
        assertTrue(casos.size() > 200, "el corpus generado no esta donde se lo busca");
        assertEquals(List.of(), fallas);
    }

    private List<String> ejecutar(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(caso.toString());
        return salida.getLineas();
    }
}
