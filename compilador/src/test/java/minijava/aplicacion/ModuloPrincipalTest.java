package minijava.aplicacion;

import minijava.config.ConfiguracionDelCompilador;
import minijava.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuloPrincipalTest {

    private Path escribir(Path dir, String contenido) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, contenido);
        return archivo;
    }

    // Oráculo copiado literal del ejemplo de Pautas Etapa 1 (slide 26), no
    // derivado de la implementación.
    @Test
    void ejemploCompletoDeLaCatedraSinErrores(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "\"hola\"\nv1 + chau\nif class}");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(archivo.toString());

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
                "[SinErrores]"
        ), lineas);
    }

    // Oráculo copiado literal del ejemplo de Pautas Etapa 1 (slide 25).
    @Test
    void ejemploCompletoDeLaCatedraConError(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "\"hola\"\nv1 + # chau\nif class}");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.contains("[Error:#|2]"));
    }

    // Logro Multi-detección de Errores: no se detiene en el primer error,
    // reporta todos los que encuentra en una sola corrida.
    @Test
    void reportaTodosLosErroresDeUnaCorridaNoSoloElPrimero(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "v1 # v2 @ v3");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.contains("[Error:#|1]"));
        assertTrue(lineas.contains("[Error:@|1]"));
    }

    @Test
    void siHuboErroresNoImprimeSinErrores(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "#");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(archivo.toString());

        assertTrue(salida.getLineas().stream().noneMatch("[SinErrores]"::equals));
    }

    @Test
    void continuaReconociendoTokensValidosDespuesDeCadaError(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "v1 # v2");
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.stream().anyMatch(l -> l.contains("idMV,v1,")));
        assertTrue(lineas.stream().anyMatch(l -> l.contains("idMV,v2,")));
    }

    // Logro Multi-detección de Errores desactivado: comportamiento base de
    // la cátedra ("al detectar un error deberá reportarse y finalizar la
    // ejecución") — se detiene en el PRIMER error, no reporta el segundo.
    @Test
    void conMultiDeteccionDesactivadaSeDetieneEnElPrimerError(@TempDir Path dir) throws IOException {
        Path archivo = escribir(dir, "v1 # v2 @ v3");
        SalidaEnMemoria salida = new SalidaEnMemoria();
        var configuracion = new ConfiguracionDelCompilador(true, true, true, false);

        new ModuloPrincipal(salida, configuracion).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.contains("[Error:#|1]"));
        assertTrue(lineas.stream().noneMatch(l -> l.equals("[Error:@|1]")));
        assertTrue(lineas.stream().noneMatch(l -> l.contains("idMV,v2,")));
    }

    @Test
    void conReporteEleganteDesactivadoSoloImprimeElCodigoDeContrato(@TempDir Path dir)
            throws IOException {
        Path archivo = escribir(dir, "v1 # v2");
        SalidaEnMemoria salida = new SalidaEnMemoria();
        var configuracion = new ConfiguracionDelCompilador(true, true, false, true);

        new ModuloPrincipal(salida, configuracion).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.contains("[Error:#|1]"));
        assertTrue(lineas.stream().noneMatch(l -> l.startsWith("Error Léxico")));
        assertTrue(lineas.stream().noneMatch(l -> l.startsWith("Detalle:")));
    }

    // Regresión: ModuloPrincipal creaba el AnalizadorLexico con el
    // constructor de un solo argumento, que usa ConfiguracionDelCompilador
    // .DEFAULT — la configuración que recibía de su caller nunca llegaba al
    // léxico. Los dos tests de configuración de arriba no lo detectaban
    // porque multiDeteccionDeErrores y reporteElegante se resuelven fuera del
    // léxico; estos dos ejercitan los dos flags que sí lee el autómata.
    @Test
    void laConfiguracionLlegaAlLexicoConLosFloatsDeshabilitados(@TempDir Path dir)
            throws IOException {
        Path archivo = escribir(dir, "1.5");
        SalidaEnMemoria salida = new SalidaEnMemoria();
        var configuracion = new ConfiguracionDelCompilador(false, true, true, true);

        new ModuloPrincipal(salida, configuracion).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertEquals(List.of(
                "(intLiteral,1,1)",
                "(punto,.,1)",
                "(intLiteral,5,1)",
                "(EOF,$,1)",
                "[SinErrores]"
        ), lineas);
    }

    @Test
    void laConfiguracionLlegaAlLexicoConElUnicodeDeshabilitado(@TempDir Path dir)
            throws IOException {
        Path archivo = escribir(dir, "\"a\\u0041\"");
        SalidaEnMemoria salida = new SalidaEnMemoria();
        var configuracion = new ConfiguracionDelCompilador(true, false, true, true);

        new ModuloPrincipal(salida, configuracion).ejecutar(archivo.toString());

        List<String> lineas = salida.getLineas();
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("[Error:")));
    }
}
