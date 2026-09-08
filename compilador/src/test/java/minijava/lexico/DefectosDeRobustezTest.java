package minijava.lexico;

import minijava.aplicacion.ModuloPrincipalLexico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Regresiones de tres defectos de robustez encontrados auditando el léxico: todo
// fallo que no sea una ExcepcionLexica controlada es un defecto, no comportamiento.
@Tag("lexico")
class DefectosDeRobustezTest {

    private List<String> ejecutar(Path archivo) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // El centinela de la cátedra es (char) 26, un byte válido dentro de un archivo.
    // Antes, un 0x1A embebido cortaba el análisis ahí e imprimía [SinErrores].
    @Test
    void unByteIgualAlCentinelaDeEofNoTruncaElAnalisis(@TempDir Path dir) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, "v1" + (char) 26 + "\nv2\n");

        List<String> lineas = ejecutar(archivo);

        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,2)"), lineas::toString);
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("[Error:")), lineas::toString);
        assertFalse(lineas.contains("[SinErrores]"), lineas::toString);
    }

    // Un archivo no-UTF-8 reventaba con UncheckedIOException: un stack trace por
    // stderr, violando además que todo sale por System.out.
    @Test
    void unArchivoConBytesNoUtf8NoRevientaElCompilador(@TempDir Path dir) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.write(archivo, new byte[]{'v', '1', ' ', (byte) 0xFF, (byte) 0xFE, ' ', 'v', '2'});

        List<String> lineas = ejecutar(archivo);

        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("[Error:")), lineas::toString);
    }

    // Dos invariantes: ninguna línea de salida lleva un salto o retorno (parten la
    // línea en dos), y el lexema de un error no lleva ningún control (se escapa).
    // "Detalle:" queda afuera de la segunda: escaparla desalinearía el '^'.
    @Test
    void losCaracteresDeControlNuncaSalenCrudosEnElReporte(@TempDir Path dir) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        // No son blancos en MiniJava, así que son errores y su lexema sale escapado.
        Files.writeString(archivo, "v1 " + (char) 12 + " " + (char) 11 + " v2\n",
                StandardCharsets.UTF_8);

        List<String> lineas = ejecutar(archivo);

        assertTrue(lineas.stream().noneMatch(l -> l.indexOf('\n') >= 0 || l.indexOf('\r') >= 0),
                lineas::toString);
        assertTrue(lineas.stream().filter(l -> l.startsWith("[Error:"))
                        .noneMatch(l -> l.chars().anyMatch(c -> c < 0x20)),
                lineas::toString);
        assertTrue(lineas.contains("[Error:\\f|1]"), lineas::toString);
        assertTrue(lineas.contains("[Error:\\u000b|1]"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
    }
}
