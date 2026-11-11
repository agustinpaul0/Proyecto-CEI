package catedra;

import minijava.aplicacion.Main;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

// El tester de la cátedra para la Etapa 5 (TestersGeneracion.zip), con su
// criterio intacto: compila cada caso, corre el .out con la CeIVM en un proceso
// aparte y exige que su salida contenga cada fragmento de la primera línea,
// `///1234&exitosamente`. Sólo cambian el paquete, la clase del main, la carpeta
// de los casos y la categoría. Lo que el criterio deja pasar lo endurece
// minijava.generacion.EjecucionDelCorpusTest.
@RunWith(Parameterized.class)
@Category(EtapaGeneracion.class)
public class TesterDeCasosGeneracionSinErrores {

    private static final String testFilesDirectoryPath = "resources/catedra/generacion/";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpClass() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDownClass() {
        System.setOut(originalOut);
    }

    @Parameters(name = "{0}")
    public static Iterable<? extends Object> data() {
        File folder = new File(testFilesDirectoryPath);
        ArrayList<String> names = new ArrayList<>();
        for (File f : folder.listFiles()) {
            names.add(f.getName());
        }
        names.sort(String::compareTo);
        return names;
    }

    private final String input;

    public TesterDeCasosGeneracionSinErrores(String input) {
        this.input = input;
    }

    @Test
    public void testIterado() {
        probarExito(input);
    }

    void probarExito(String name) {
        String path = testFilesDirectoryPath + name;
        String[] expectedValues = getExpectedValues(path);

        List<Matcher<? super String>> expectedValuesMatchers = new ArrayList<>();
        for (String s : expectedValues) {
            expectedValuesMatchers.add(CoreMatchers.containsString(s));
        }
        String generatedFileName = "[" + name + "].out";
        String[] args = {path, generatedFileName};
        Main.main(args);
        String result;
        try {
            Process proc = new ProcessBuilder("java", "-jar", "CeIVM-cei2011.jar", generatedFileName).start();
            result = new BufferedReader(new InputStreamReader(proc.getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
            result += new BufferedReader(new InputStreamReader(proc.getErrorStream())).lines()
                    .collect(Collectors.joining("\n"));
            result += "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
            proc.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setOut(originalOut);
        System.out.println(outContent);
        System.out.println(result);

        assertThat("Mensaje Incorrecto en: " + path, result, CoreMatchers.allOf(expectedValuesMatchers));
    }

    String[] getExpectedValues(String path) {
        String lineWithTheCode;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            lineWithTheCode = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lineWithTheCode.substring(3).split("&");
    }
}
