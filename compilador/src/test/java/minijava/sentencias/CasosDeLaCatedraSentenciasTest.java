package minijava.sentencias;

import minijava.aplicacion.Main;
import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los fuentes que la cátedra adjunta en testersSemantico2, copiados a
// resources/catedra/sentencias/. El compilador tiene que dar exactamente el
// código que cada uno declara en su primera línea, y nada más.
//
// semIICorrecto03 traía `a1++ + 10`; el profesor indicó que va `a1+4+ + 10`.
@Tag("sentencias")
class CasosDeLaCatedraSentenciasTest {

    private static final Path RAIZ = Path.of("resources", "catedra", "sentencias");

    @ParameterizedTest
    @ValueSource(strings = {
            "sinErrores/semIICorrecto01.java",
            "sinErrores/semIICorrecto02.java",
            "sinErrores/semIICorrecto03.java",
            "sinErrores/semIICorrecto04.java",
            "sinErrores/semIICorrecto05.java",
            "conErrores/semIIError01.java",
            "conErrores/semIIError02.java",
            "conErrores/semIIError03.java",
            "conErrores/semIIError04.java",
            "conErrores/semIIError05.java",
    })
    void cadaCasoDaExactamenteElCodigoQueLaCatedraDeclara(String caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(RAIZ.resolve(caso).toString());
        List<String> lineas = salida.getLineas();

        assertEquals(3, lineas.size(), lineas.toString());
        assertEquals(codigoEsperado(caso), lineas.getLast());
    }

    // El tester de la cátedra llama a main() sólo con la ruta: la etapa por
    // defecto tiene que ser la del chequeo de sentencias.
    @Test
    void sinIndicarLaEtapaCorreElChequeoDeSentencias() {
        PrintStream consola = System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[]{RAIZ.resolve("conErrores/semIIError01.java").toString()});
        } finally {
            System.setOut(consola);
        }
        assertTrue(capturada.toString(StandardCharsets.UTF_8).contains("[Error:v1|9]"), capturada.toString());
    }

    // Los casos de la cátedra marcan el código con una, dos o tres barras.
    private String codigoEsperado(String caso) throws IOException {
        if (caso.startsWith("sinErrores")) {
            return "[SinErrores]";
        }
        return Files.readAllLines(RAIZ.resolve(caso)).getFirst().strip().replaceFirst("^/+", "");
    }
}
