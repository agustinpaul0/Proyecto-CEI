package catedra;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import minijava.aplicacion.Main;
import minijava.casos.VolcadoDeCasos;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Tester de la cátedra, con su criterio intacto: la salida tiene que CONTENER el
// código de error declarado en la primera línea del caso. Se deja sin endurecer
// para mostrar que la entrega pasa su batería tal como ella la escribió;
// minijava.casos.TesterDeCasosConErroresEstricto verifica lo demás.
@RunWith(Parameterized.class)
public class TesterDeCasosConErrores {

    private static final String TEST_FILES_DIRECTORY_PATH = "resources/lexico/conErrores/";
    // La primera línea es "// [Error:Lexema|NroLinea]": se saltea el "// ".
    private static final int LARGO_DEL_PREFIJO_DE_COMENTARIO = 3;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    // Estática y capturada al cargar la clase: setUp() redirige System.out para
    // capturar al compilador, y @AfterClass no ve los campos de instancia.
    private static final PrintStream CONSOLA = System.out;

    private final String input;

    public TesterDeCasosConErrores(String input) {
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

    @Parameters(name = "{0}")
    public static Iterable<String> data() {
        File folder = new File(TEST_FILES_DIRECTORY_PATH);
        File[] files = folder.listFiles();

        if (files == null) {
            throw new RuntimeException(
                    "No se pudo leer el directorio de casos de prueba: "
                            + TEST_FILES_DIRECTORY_PATH
                            + ". Los tests se corren desde la carpeta del pom.xml."
            );
        }

        List<String> names = new ArrayList<>();

        for (File file : files) {
            if (file.isFile()) {
                names.add(file.getName());
            }
        }

        names.sort(String::compareTo);
        return names;
    }

    @AfterClass
    public static void informarElCorpusProcesado() {
        VolcadoDeCasos.resumirDirectorio(CONSOLA, TEST_FILES_DIRECTORY_PATH);
    }

    @Test
    public void testIterado() {
        probarFallo(input);
    }

    private void probarFallo(String name) {
        String path = TEST_FILES_DIRECTORY_PATH + name;
        String errorCode = getErrorCode(path);

        invocarCompilador(path);

        String salida = outContent.toString();

        // CONSOLA y no System.out: setUp() lo tiene redirigido para capturar al
        // compilador. Qué imprime lo decide -Dvolcado (VolcadoDeCasos).
        VolcadoDeCasos.volcar(CONSOLA, path, salida);

        assertThat(
                "No se encontro el codigo: " + errorCode + " en: " + path
                        + "\nSalida obtenida:\n" + salida,
                salida,
                CoreMatchers.containsString(errorCode)
        );
    }

    // Invocado in-process una vez por caso. Si el main se muda, se ajusta acá.
    private void invocarCompilador(String rutaDelFuente) {
        Main.main(new String[] { "--etapa=1", rutaDelFuente });
    }

    /*
     * La expectativa de un caso con errores se escribe en la PRIMERA linea del
     * archivo fuente, como comentario, con este formato estricto:
     *
     * // [Error:<lexema>|<numeroDeLinea>]
     *
     * Ejemplo:
     *
     * // [Error:#|2]
     * class A {
     * # }
     *
     * El lexema es el armado hasta el momento del error, tal como lo pide
     * Pautas Etapa 1. No lleva columna: la columna, cuando el compilador la
     * informa, va solo en el mensaje legible que antecede al codigo.
     */
    String getErrorCode(String testCaseFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(testCaseFilePath))) {
            String lineWithTheCode = reader.readLine();

            if (lineWithTheCode == null || lineWithTheCode.length() < LARGO_DEL_PREFIJO_DE_COMENTARIO) {
                throw new RuntimeException(
                        "La primera linea de " + testCaseFilePath
                                + " deberia declarar el codigo de error esperado,"
                                + " con el formato: // [Error:<lexema>|<numeroDeLinea>]"
                );
            }

            return lineWithTheCode.substring(LARGO_DEL_PREFIJO_DE_COMENTARIO);

        } catch (IOException exception) {
            throw new RuntimeException("No se pudo leer el caso de prueba: " + testCaseFilePath, exception);
        }
    }
}
