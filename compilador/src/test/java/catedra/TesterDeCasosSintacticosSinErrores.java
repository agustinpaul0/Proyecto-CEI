package catedra;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import minijava.aplicacion.Main;
import minijava.casos.VolcadoDeCasos;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;

// El tester de la cátedra para la Etapa 2, con su criterio intacto: un fuente
// correcto tiene que producir una salida que CONTENGA [SinErrores]. No verifica
// tokens ni lexemas, a diferencia del de la Etapa 1.
//
// minijava.casos.TesterDeCasosSintacticosEstricto endurece lo demás.
@RunWith(Parameterized.class)
@Tag("sintactico")
public class TesterDeCasosSintacticosSinErrores {

    private static final String MENSAJE_DE_EXITO = "[SinErrores]";
    private static final String TEST_FILES_DIRECTORY_PATH = "resources/sintactico/sinErrores/";
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream CONSOLA = System.out;

    private final String input;

    public TesterDeCasosSintacticosSinErrores(String input) {
        this.input = input;
    }

    @Before
    public void setUp() {
        outContent.reset();
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDown() {
        System.setOut(CONSOLA);
    }

    @AfterClass
    public static void informarElCorpusProcesado() {
        VolcadoDeCasos.resumirDirectorio(CONSOLA, TEST_FILES_DIRECTORY_PATH);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<String> data() {
        File carpeta = new File(TEST_FILES_DIRECTORY_PATH);
        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            throw new RuntimeException(
                    "No se pudo leer el directorio de casos de prueba: " + TEST_FILES_DIRECTORY_PATH
                            + ". Los tests se corren desde la carpeta del pom.xml.");
        }

        List<String> nombres = new ArrayList<>();
        for (File archivo : archivos) {
            if (archivo.isFile()) {
                nombres.add(archivo.getName());
            }
        }
        Collections.sort(nombres);
        return nombres;
    }

    @Test
    public void testIterado() {
        probarExito(input);
    }

    private void probarExito(String nombre) {
        String ruta = TEST_FILES_DIRECTORY_PATH + nombre;

        Main.main(new String[] { "--etapa=2", ruta });

        String salida = outContent.toString();
        VolcadoDeCasos.volcar(CONSOLA, ruta, salida);

        assertThat("Mensaje incorrecto en: " + ruta + "\nSalida obtenida:\n" + salida,
                salida, CoreMatchers.containsString(MENSAJE_DE_EXITO));
    }

}
