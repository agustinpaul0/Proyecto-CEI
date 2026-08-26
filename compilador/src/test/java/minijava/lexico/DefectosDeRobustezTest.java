package minijava.lexico;

import minijava.aplicacion.ModuloPrincipal;
import minijava.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Regresiones permanentes de tres defectos de robustez encontrados auditando
// el léxico. Los tres caían bajo la regla de agents/testing.md: cualquier
// fallo que no sea una ExcepcionLexica controlada es un defecto del
// compilador, nunca comportamiento esperado.
class DefectosDeRobustezTest {

    private List<String> ejecutar(Path archivo) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipal(salida).ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // El centinela de fin de archivo de la cátedra es (char) 26, que también
    // es un byte perfectamente válido dentro de un archivo. Antes de este
    // arreglo un 0x1A embebido hacía que el análisis TERMINARA ahí mismo: el
    // resto del archivo desaparecía sin un solo mensaje, y encima se imprimía
    // [SinErrores] como si todo hubiera salido bien.
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

    // Un archivo con bytes que no son UTF-8 válido reventaba con
    // UncheckedIOException(MalformedInputException): un stack trace crudo por
    // stderr, violando además la regla de que todo sale por System.out.
    @Test
    void unArchivoConBytesNoUtf8NoRevientaElCompilador(@TempDir Path dir) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.write(archivo, new byte[]{'v', '1', ' ', (byte) 0xFF, (byte) 0xFE, ' ', 'v', '2'});

        List<String> lineas = ejecutar(archivo);

        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("[Error:")), lineas::toString);
    }

    // Dos invariantes distintas, que conviene no confundir:
    //
    //  a) NINGUNA línea de salida puede contener un salto de línea o un
    //     retorno de carro: son los únicos caracteres que parten una línea en
    //     dos y rompen el contrato de la cátedra de un elemento por línea.
    //  b) El LEXEMA de un error no puede contener ningún carácter de control,
    //     porque se imprime dentro de [Error:Lexema|NroLinea]; se renderiza
    //     escapado (\f, \u000b).
    //
    // La línea "Detalle:" queda deliberadamente afuera de (b): es un eco
    // literal de la línea fuente, y escaparla desalinearía el '^', que se
    // posiciona contando caracteres del fuente original.
    @Test
    void losCaracteresDeControlNuncaSalenCrudosEnElReporte(@TempDir Path dir) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        // Ni el form feed ni el tabulador vertical estan en el conjunto
        // de blancos de MiniJava (a diferencia de Java), asi que son
        // errores, y su lexema tiene que salir escapado, no crudo.
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
