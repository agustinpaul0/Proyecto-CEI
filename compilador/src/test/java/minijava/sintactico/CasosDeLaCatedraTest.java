package minijava.sintactico;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los 8 fuentes que la cátedra adjunta en testersSintactico, copiados a
// resources/catedra/sintactico/. Cada uno declara en su primera línea el código
// que espera, y el compilador tiene que darlo exacto: no alcanza con aceptar o
// rechazar, el error tiene que caer en el mismo token.
@Tag("sintactico")
class CasosDeLaCatedraTest {

    private static final Path RAIZ = Path.of("resources", "catedra", "sintactico");

    private static final List<String> CASOS = List.of(
            "sinErrores/sintCorrecto01.java", "sinErrores/sintCorrecto02.java",
            "sinErrores/sintCorrecto03.java", "sinErrores/sintCorrecto04.java",
            "conErrores/sintError01.java", "conErrores/sintError02.java",
            "conErrores/sintError03.java", "conErrores/sintError04.java");

    private List<String> ejecutar(String caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida).ejecutar(RAIZ.resolve(caso).toString());
        return salida.getLineas();
    }

    private String codigoDeclarado(String caso) throws IOException {
        return Files.readAllLines(RAIZ.resolve(caso)).getFirst().replaceFirst("^/+", "");
    }

    @ParameterizedTest
    @CsvSource({
            "sinErrores/sintCorrecto01.java",
            "sinErrores/sintCorrecto02.java",
            "sinErrores/sintCorrecto03.java",
            "sinErrores/sintCorrecto04.java",
            "conErrores/sintError01.java",
            "conErrores/sintError02.java",
            "conErrores/sintError03.java",
            "conErrores/sintError04.java",
    })
    void cadaCasoDaExactamenteElCodigoQueLaCatedraDeclara(String caso) throws Exception {
        assertEquals(codigoDeclarado(caso), primerCodigo(ejecutar(caso)));
    }

    // El primero, y no el último: con Modo Pánico el análisis sigue después del
    // primer error y puede reportar los que arrastra. El que la cátedra declara
    // es el primero.
    private String primerCodigo(List<String> salida) {
        return salida.stream().filter(linea -> linea.startsWith("[")).findFirst()
                .orElseThrow(() -> new AssertionError("no se emitio ningun codigo: " + salida));
    }

    @Test
    void ningunCasoDeLaCatedraHaceQueElCompiladorSeCaiga() throws Exception {
        for (String directorio : List.of("sinErrores", "conErrores")) {
            try (var archivos = Files.list(RAIZ.resolve(directorio))) {
                for (Path caso : archivos.filter(Files::isRegularFile).toList()) {
                    List<String> salida = ejecutar(directorio + "/" + caso.getFileName());
                    assertTrue(salida.getLast().equals("[SinErrores]")
                                    || salida.getLast().startsWith("[Error:"),
                            caso + " -> " + salida);
                }
            }
        }
    }
}
