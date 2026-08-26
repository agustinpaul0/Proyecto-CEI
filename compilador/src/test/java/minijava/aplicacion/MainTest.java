package minijava.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Main es deliberadamente delgado y toca System.out real (agents/testing.md),
// pero sus modos de falla SÍ tienen que estar cubiertos: los testers de la
// cátedra invocan main() in-process y repetidamente dentro de la misma JVM,
// así que una excepción sin atrapar mata la corrida entera de tests.
class MainTest {

    private String capturarSalidaDe(String... args) {
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
        try {
            Main.main(args);
        } finally {
            System.setOut(original);
        }
        return capturada.toString(StandardCharsets.UTF_8);
    }

    // Antes tiraba ArrayIndexOutOfBoundsException: args[0] estaba dentro de
    // un try que sólo capturaba IOException.
    @Test
    void sinArgumentosInformaElUsoYNoLanzaExcepcion() {
        String salida = capturarSalidaDe();

        assertTrue(salida.contains("Uso:"), salida);
    }

    @Test
    void conUnArchivoInexistenteInformaElProblemaYNoLanzaExcepcion(@TempDir Path dir) {
        String salida = capturarSalidaDe(dir.resolve("no-existe.mjava").toString());

        assertTrue(salida.contains("No se pudo leer el archivo fuente"), salida);
    }

    // La cátedra exige aceptar cualquier extensión, no sólo .java
    // (agents/convenciones-catedra.md).
    @Test
    void aceptaCualquierExtensionDeArchivo(@TempDir Path dir) throws Exception {
        Path archivo = dir.resolve("fuente.loquesea");
        Files.writeString(archivo, "v1\n");

        String salida = capturarSalidaDe(archivo.toString());

        assertTrue(salida.contains("(idMV,v1,1)"), salida);
        assertTrue(salida.contains("[SinErrores]"), salida);
    }
}
