package minijava.sentencias;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.salida.SalidaEnMemoria;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Tag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Fuzzing por mutación a ciegas sobre los fuentes correctos del corpus de la
// Etapa 4: se borran, duplican, intercambian o reemplazan palabras. Lo que sale
// casi nunca es válido, y no se predice el código: el compilador tiene que
// terminar siempre en uno, sin excepciones, como pide la Pautas Etapa 4 slide 30.
@Tag("sentencias")
class SentenciasFuzzTest {

    private static final Path ARCHIVO = crearArchivo();
    private static final List<String> FUENTES = cargarFuentes();
    private static final List<String> REEMPLAZOS = List.of(
            "this", "null", "true", "1", "'c'", "\"s\"", "new", "var", "return", "if", "while", "=", "==",
            "+", "!", ".", "[", "]", "(", ")", "{", "}", ";", "x", "Aa", "int", "void", "static", "T",
            "length", "toString", "System", "Object", "String");

    private static Path crearArchivo() {
        try {
            Path archivo = Files.createTempFile("minijava-fuzz-", ".java");
            archivo.toFile().deleteOnExit();
            return archivo;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private static List<String> cargarFuentes() {
        try (Stream<Path> archivos = Files.list(Path.of("resources", "sinErrores"))) {
            List<String> fuentes = new ArrayList<>();
            for (Path archivo : archivos.sorted().toList()) {
                fuentes.add(Files.readString(archivo));
            }
            return fuentes;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    @Property(tries = 3000, seed = "20261017")
    void unaMutacionNuncaHaceCaerAlCompilador(@ForAll long semilla) {
        Random azar = new Random(semilla);
        String mutado = mutar(FUENTES.get(azar.nextInt(FUENTES.size())), azar);
        List<String> salida = compilar(mutado);

        assertFalse(salida.isEmpty(), mutado);
        String ultima = salida.getLast();
        assertTrue(ultima.equals("[SinErrores]") || ultima.startsWith("[Error:"), mutado + "\n" + salida);
    }

    private String mutar(String fuente, Random azar) {
        List<String> palabras = new ArrayList<>(Arrays.asList(fuente.split(" ")));
        int cambios = 1 + azar.nextInt(4);
        for (int i = 0; i < cambios && palabras.size() > 1; i++) {
            int posicion = azar.nextInt(palabras.size());
            switch (azar.nextInt(4)) {
                case 0 -> palabras.remove(posicion);
                case 1 -> palabras.add(posicion, palabras.get(posicion));
                case 2 -> palabras.set(posicion, REEMPLAZOS.get(azar.nextInt(REEMPLAZOS.size())));
                default -> {
                    int otra = azar.nextInt(palabras.size());
                    String guardada = palabras.get(posicion);
                    palabras.set(posicion, palabras.get(otra));
                    palabras.set(otra, guardada);
                }
            }
        }
        return String.join(" ", palabras);
    }

    private List<String> compilar(String fuente) {
        try {
            Files.writeString(ARCHIVO, fuente);
            SalidaEnMemoria salida = new SalidaEnMemoria();
            ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(ARCHIVO.toString());
            return salida.getLineas();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
