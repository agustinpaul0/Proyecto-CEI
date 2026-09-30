package minijava.semantico;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Los 8 fuentes que la cátedra adjunta en testersSemantico1, copiados a
// resources/catedra/semantico/. El compilador tiene que dar exactamente el
// código que cada uno declara en su primera línea, y nada más.
@Tag("semantico")
class CasosDeLaCatedraSemanticoTest {

    private static final Path RAIZ = Path.of("resources", "catedra", "semantico");

    @ParameterizedTest
    @ValueSource(strings = {
            "sinErrores/semICorrecto01.java",
            "sinErrores/semICorrecto02.java",
            "sinErrores/semICorrecto03.java",
            "sinErrores/semICorrecto04.java",
            "sinErrores/semICorrecto05.java",
            "conErrores/semIError01.java",
            "conErrores/semIError02.java",
            "conErrores/semIError03.java",
    })
    void cadaCasoDaExactamenteElCodigoQueLaCatedraDeclara(String caso) throws IOException {
        List<String> salida = ejecutar(caso);

        assertEquals(3, salida.size(), salida.toString());
        assertEquals(codigoDeclarado(caso), salida.getLast());
    }

    private List<String> ejecutar(String caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSemantico(salida).ejecutar(RAIZ.resolve(caso).toString());
        return salida.getLineas();
    }

    private String codigoDeclarado(String caso) throws IOException {
        return Files.readAllLines(RAIZ.resolve(caso)).getFirst().replaceFirst("^/+", "");
    }
}
