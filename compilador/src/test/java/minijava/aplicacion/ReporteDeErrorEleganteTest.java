package minijava.aplicacion;

import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class ReporteDeErrorEleganteTest {

    private Path escribir(Path dir, String contenido) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, contenido);
        return archivo;
    }

    // Oráculo literal del ejemplo de Pautas Etapa 1 slide 43.
    @Test
    void muestraLaLineaCompletaConUnCaretApuntandoAlError(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "\"hola\"\nv1 + # chau\nif class}");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        int i = lineas.indexOf("[Error:#|2]");
        assertEquals("Error Léxico en línea 2, columna 6: # no es un símbolo válido", lineas.get(i - 3));
        assertEquals("Detalle: v1 + # chau", lineas.get(i - 2));
        assertEquals("              ^", lineas.get(i - 1));
    }
}
