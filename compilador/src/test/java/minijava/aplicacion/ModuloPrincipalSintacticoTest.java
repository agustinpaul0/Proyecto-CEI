package minijava.aplicacion;

import minijava.comun.salida.SalidaEnMemoria;
import minijava.logros.ConfiguracionDeLaCorrida;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("sintactico")
class ModuloPrincipalSintacticoTest {

    private List<String> ejecutar(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida, ConfiguracionDeLaCorrida.actual())
                .ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // Pautas Etapa 2, slide 27: mensaje adecuado y luego el código de éxito.
    @Test
    void unFuenteValidoInformaExitoConElCodigoDeLaCatedra(@TempDir Path dir) throws Exception {
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"),
                ejecutar(dir, "class C1 {}\n"));
    }

    // Pautas Etapa 2, slide 25: el código de error es la ÚLTIMA línea del reporte.
    @Test
    void unFuenteInvalidoInformaElCodigoDeErrorComoUltimaLinea(@TempDir Path dir) throws Exception {
        List<String> salida = ejecutar(dir, "class {\n\n}\n");

        assertEquals("[Error:{|1]", salida.getLast());
        assertTrue(salida.getFirst().startsWith("Error Sintactico en linea 1:"), salida.toString());
        assertEquals("", salida.get(1));
    }

    @Test
    void unFuenteInvalidoNoInformaAnalisisExitoso(@TempDir Path dir) throws Exception {
        assertTrue(!ejecutar(dir, "class {}").contains("[SinErrores]"));
    }

    // Un error léxico dentro del análisis sintáctico se sigue reportando como
    // error léxico: es la misma cadena de módulos de la Etapa 1.
    @Test
    void unErrorLexicoSeReportaConElFormatoDelLexico(@TempDir Path dir) throws Exception {
        List<String> salida = ejecutar(dir, "class C1 # {}");

        assertEquals("[Error:#|1]", salida.getLast());
    }

    @Test
    void ningunaLineaDeSalidaLlevaUnSaltoDeLineaEmbebido(@TempDir Path dir) throws Exception {
        for (String fuente : List.of("class C1 {}", "class {}", "class C1 # {}", "")) {
            for (String linea : ejecutar(dir, fuente)) {
                assertTrue(linea.indexOf('\n') < 0 && linea.indexOf('\r') < 0, fuente + " -> " + linea);
            }
        }
    }

    @Test
    void unArchivoInexistenteLanzaIOExceptionYNoRompeElReporte(@TempDir Path dir) {
        var modulo = new ModuloPrincipalSintactico(new SalidaEnMemoria(),
                ConfiguracionDeLaCorrida.actual());

        assertThrows(IOException.class, () -> modulo.ejecutar(dir.resolve("no-existe").toString()));
    }
}
