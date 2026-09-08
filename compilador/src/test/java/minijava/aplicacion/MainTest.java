package minijava.aplicacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Main es delgado y toca System.out real, pero sus modos de falla tienen que estar
// cubiertos: una excepción sin atrapar mata la corrida entera de los testers.
@Tag("lexico")
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

    // Antes tiraba ArrayIndexOutOfBoundsException: args[0] caía en un try que sólo
    // capturaba IOException.
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

    // La cátedra exige aceptar cualquier extensión, no sólo .java, en las dos etapas.
    @Test
    void aceptaCualquierExtensionDeArchivo(@TempDir Path dir) throws Exception {
        Path archivo = dir.resolve("fuente.loquesea");
        Files.writeString(archivo, "class C1 {}\n");

        assertTrue(capturarSalidaDe(archivo.toString()).contains("[SinErrores]"));
        assertTrue(capturarSalidaDe("--etapa=1", archivo.toString()).contains("[SinErrores]"));
    }

    @Test
    void sinEtapaCorreElAnalizadorSintactico(@TempDir Path dir) throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, "class C1 {}\n");

        String salida = capturarSalidaDe(archivo.toString());

        assertTrue(salida.contains("Compilacion Exitosa"), salida);
        assertFalse(salida.contains("(pr_class,class,1)"), salida);
    }

    @Test
    void conEtapaUnoVuelveAlVolcadoDeTokens(@TempDir Path dir) throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, "class C1 {}\n");

        String salida = capturarSalidaDe("--etapa=1", archivo.toString());

        assertTrue(salida.contains("(pr_class,class,1)"), salida);
        assertTrue(salida.contains("(EOF,$,2)"), salida);
    }

    @Test
    void unaOpcionDesconocidaSeInformaYNoImpideElAnalisis(@TempDir Path dir) throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, "class C1 {}\n");

        String salida = capturarSalidaDe("--inventada", archivo.toString());

        assertTrue(salida.contains("--inventada"), salida);
        assertTrue(salida.contains("[SinErrores]"), salida);
    }
}
