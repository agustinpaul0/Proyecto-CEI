package minijava.sintactico;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.salida.SalidaEnMemoria;
import minijava.logros.ConfiguracionDeLaCorrida;
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

// Los 8 fuentes que la cátedra adjunta en testersSintactico. Cada uno declara en
// su primera línea el código que espera.
//
// La Sesión 2 implementa el núcleo de la gramática, no los miembros, así que hoy
// sólo se puede exigir el veredicto de los casos que se resuelven antes de
// entrar a un miembro. Los otros cinco quedan cubiertos cuando la Sesión 3
// complete <Miembro>; hasta entonces se verifica lo único que corresponde: que
// el análisis llegue hasta donde el núcleo alcanza y falle en un miembro, no
// antes. Contra la gramática los 8 ya dan el token exacto:
//     python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java
@Tag("sintactico")
class CasosDeLaCatedraTest {

    private static final Path RAIZ = Path.of("..", "vendor", "testersSintactico", "resources");

    private List<String> ejecutar(String caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida, ConfiguracionDeLaCorrida.actual())
                .ejecutar(RAIZ.resolve(caso).toString());
        return salida.getLineas();
    }

    private String codigoDeclarado(String caso) throws IOException {
        return Files.readAllLines(RAIZ.resolve(caso)).getFirst().replaceFirst("^/+", "");
    }

    @ParameterizedTest
    @CsvSource({
            "sinErrores/sintCorrecto01.java",
            "conErrores/sintError01.java",
            "conErrores/sintError02.java",
    })
    void losCasosQueElNucleoResuelveDanElVeredictoQueLaCatedraDeclara(String caso) throws Exception {
        assertEquals(codigoDeclarado(caso), ejecutar(caso).getLast());
    }

    // El núcleo consume la cabecera de la clase y frena en su primer miembro: si
    // fallara antes, el defecto estaría en lo que la Sesión 2 sí implementa.
    @ParameterizedTest
    @CsvSource({
            "sinErrores/sintCorrecto02.java, 7",
            "sinErrores/sintCorrecto03.java, 6",
            "sinErrores/sintCorrecto04.java, 6",
            "conErrores/sintError03.java,    4",
            "conErrores/sintError04.java,    4",
    })
    void losCasosConMiembrosFrenanReciénEnElPrimerMiembro(String caso, int linea) throws Exception {
        List<String> salida = ejecutar(caso);

        assertTrue(salida.getLast().endsWith("|" + linea + "]"), caso + " -> " + salida.getLast());
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
