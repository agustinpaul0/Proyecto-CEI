package catedra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import minijava.aplicacion.Main;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Tester provisto por la cátedra. Se conserva su criterio de verificación tal
// cual: por cada archivo de resources/conErrores/ se ejecuta el compilador y se
// comprueba que su salida CONTENGA el código de error declarado en la primera
// línea del propio archivo.
//
// Ese criterio deja fuera dos cosas: un caso con varios errores queda
// verificado sólo por el primero, y no se comprueba qué tokens salen entre
// medio. Por eso el proyecto agrega minijava.casos.TesterDeCasosConErroresEstricto,
// que corre sobre los MISMOS archivos y verifica la salida completa. Este
// tester se deja al lado, sin endurecerlo, para poder mostrar que la entrega
// pasa la batería de la cátedra tal como ella la escribió.
@RunWith(Parameterized.class)
public class TesterDeCasosConErrores {

    private static final String TEST_FILES_DIRECTORY_PATH = "resources/conErrores/";
    // La primera línea del caso es "// [Error:Lexema|NroLinea]": se saltean los
    // tres caracteres de "// " para quedarse con el código.
    private static final int LARGO_DEL_PREFIJO_DE_COMENTARIO = 3;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // Cambiar a true solo para ver la salida del compilador durante la ejecucion de los tests.
    private final boolean fullCompilerOutputPrintingInEachTest = false;

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
        System.setOut(originalOut);
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

    @Test
    public void testIterado() {
        probarFallo(input);
    }

    private void probarFallo(String name) {
        String path = TEST_FILES_DIRECTORY_PATH + name;
        String errorCode = getErrorCode(path);

        invocarCompilador(path);

        String salida = outContent.toString();

        if (fullCompilerOutputPrintingInEachTest) {
            originalOut.println("----- Salida de " + path + " -----");
            originalOut.print(salida);
            originalOut.println("--------------------------------");
        }

        assertThat(
                "No se encontro el codigo: " + errorCode + " en: " + path
                        + "\nSalida obtenida:\n" + salida,
                salida,
                CoreMatchers.containsString(errorCode)
        );
    }

    // El punto de entrada del proyecto, invocado in-process una vez por caso.
    // Si la clase con el main cambia de nombre o de paquete, se ajusta el
    // import de arriba y esta única línea.
    private void invocarCompilador(String rutaDelFuente) {
        Main.main(new String[] { rutaDelFuente });
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
