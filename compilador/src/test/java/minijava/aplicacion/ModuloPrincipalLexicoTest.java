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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("lexico")
class ModuloPrincipalLexicoTest {

    private Path escribir(Path dir, String contenido) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, contenido);
        return archivo;
    }

    // Oráculo literal de Pautas Etapa 1 slide 26, no derivado de la implementación.
    @Test
    void ejemploCompletoDeLaCatedraSinErrores(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "\"hola\"\nv1 + chau\nif class}");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertEquals(List.of(
                "(litString,\"hola\",1)",
                "(idMV,v1,2)",
                "(op+,+,2)",
                "(idMV,chau,2)",
                "(pr_if,if,3)",
                "(pr_class,class,3)",
                "(llaveC,},3)",
                "(EOF,$,3)",
                "",
                "[SinErrores]"
        ), lineas);
    }

    // Oráculo copiado literal del ejemplo de Pautas Etapa 1 (slide 25).
    @Test
    void ejemploCompletoDeLaCatedraConError(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "\"hola\"\nv1 + # chau\nif class}");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.contains("[Error:#|2]"));
    }

    // Multi-detección: reporta todos los errores de una corrida, no sólo el primero.
    @Test
    void reportaTodosLosErroresDeUnaCorridaNoSoloElPrimero(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "v1 # v2 @ v3");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.contains("[Error:#|1]"));
        assertTrue(lineas.contains("[Error:@|1]"));
    }

    @Test
    void siHuboErroresNoImprimeSinErrores(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "#");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());

        assertTrue(salida.getLineas().stream().noneMatch("[SinErrores]"::equals));
    }

    @Test
    void continuaReconociendoTokensValidosDespuesDeCadaError(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "v1 # v2");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipalLexico(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.stream().anyMatch(l -> l.contains("idMV,v1,")));
        assertTrue(lineas.stream().anyMatch(l -> l.contains("idMV,v2,")));
    }

}
