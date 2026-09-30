package catedra;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
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

// El tester de la cátedra para la Etapa 3, con su criterio intacto: la salida
// tiene que CONTENER el código de error declarado en la primera línea del caso.
// Se deja sin endurecer para mostrar que la entrega pasa su batería tal como
// ella la escribió; minijava.casos.TesterDeCasosSemanticosEstricto verifica
// que además sea el ÚNICO código.
@RunWith(Parameterized.class)
@Tag("semantico")
public class TesterDeCasosSemanticosConErrores {

    private static final String TEST_FILES_DIRECTORY_PATH = "resources/conErrores/";
    // La primera línea es "///[Error:Lexema|NroLinea]": se saltea el "///".
    private static final int LARGO_DEL_PREFIJO_DE_COMENTARIO = 3;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream CONSOLA = System.out;

    private final String input;

    public TesterDeCasosSemanticosConErrores(String input) {
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
        probarFallo(input);
    }

    private void probarFallo(String nombre) {
        String ruta = TEST_FILES_DIRECTORY_PATH + nombre;

        String codigoEsperado = codigoDeError(ruta);

        Main.main(new String[] { ruta });

        String salida = outContent.toString();
        VolcadoDeCasos.volcar(CONSOLA, ruta, salida);

        assertThat("No se encontro el codigo: " + codigoEsperado + " en: " + ruta
                        + "\nSalida obtenida:\n" + salida,
                salida, CoreMatchers.containsString(codigoEsperado));
    }

    /*
     * La expectativa se escribe en la PRIMERA linea del fuente, como comentario:
     *
     *     ///[Error:<lexema>|<numeroDeLinea>]
     *
     * Es el formato de los casos que la catedra adjunta en testersSintactico.
     * El lexema del token EOF es $, como pide Pautas Etapa 2 slide 26.
     */
    String codigoDeError(String rutaDelCaso) {
        try (BufferedReader lector = new BufferedReader(new FileReader(rutaDelCaso))) {
            String primeraLinea = lector.readLine();

            if (primeraLinea == null || primeraLinea.length() < LARGO_DEL_PREFIJO_DE_COMENTARIO) {
                throw new RuntimeException(
                        "La primera linea de " + rutaDelCaso
                                + " deberia declarar el codigo de error esperado,"
                                + " con el formato: ///[Error:<lexema>|<numeroDeLinea>]");
            }

            return primeraLinea.substring(LARGO_DEL_PREFIJO_DE_COMENTARIO);

        } catch (IOException excepcion) {
            throw new RuntimeException("No se pudo leer el caso de prueba: " + rutaDelCaso,
                    excepcion);
        }
    }

}
