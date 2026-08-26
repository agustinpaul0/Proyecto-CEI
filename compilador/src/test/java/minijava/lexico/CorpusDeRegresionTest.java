package minijava.lexico;

import minijava.aplicacion.ModuloPrincipal;
import minijava.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// resources/regresion/ es donde AnalizadorLexicoFuzzTest persiste cualquier
// entrada que haya hecho fallar una corrida, para que el caso quede para
// siempre (agents/testing.md: las regresiones son append-only). Hasta ahora
// NINGÚN tester leía ese directorio, así que los casos guardados no protegían
// de nada: quedaban de adorno. Este test los reprocesa todos.
//
// No hay oráculo por caso — son entradas aleatorias, nadie sabe qué tokens
// "deberían" salir. Lo que se verifica es el contrato que sí vale para
// cualquier entrada: el compilador termina, no lanza nada que no sea una
// ExcepcionLexica controlada, y la salida respeta el formato.
class CorpusDeRegresionTest {

    private static final Path DIRECTORIO = Path.of("resources", "regresion");

    static Stream<Path> casos() throws IOException {
        if (!Files.isDirectory(DIRECTORIO)) {
            return Stream.empty();
        }
        try (var archivos = Files.list(DIRECTORIO)) {
            return archivos.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted(Comparator.naturalOrder())
                    .toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void elCasoPersistidoSigueSiendoProcesableSinFallosNoControlados(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();

        new ModuloPrincipal(salida).ejecutar(caso.toString());

        List<String> lineas = salida.getLineas();
        assertFalse(lineas.isEmpty(), () -> caso + ": no produjo ninguna salida");
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("(EOF,")),
                () -> caso + ": el analisis no llego al token EOF.\n" + String.join("\n", lineas));
        assertTrue(lineas.stream().noneMatch(l -> l.indexOf('\n') >= 0 || l.indexOf('\r') >= 0),
                () -> caso + ": una linea de salida lleva un salto de linea embebido.\n"
                        + String.join("\n", lineas));
        assertTrue(lineas.stream().filter(l -> l.startsWith("[Error:"))
                        .noneMatch(l -> l.chars().anyMatch(c -> c < 0x20)),
                () -> caso + ": un codigo de error lleva un caracter de control crudo.\n"
                        + String.join("\n", lineas));
    }

    // Guardia sobre la propia infraestructura: si alguien borra el directorio
    // o su contenido, el test de arriba pasaría vacío y en silencio.
    @Test
    void elDirectorioDeRegresionExisteYNoEstaVacio() throws IOException {
        assertTrue(Files.isDirectory(DIRECTORIO), DIRECTORIO + " no existe");
        assertTrue(casos().findAny().isPresent(),
                DIRECTORIO + " esta vacio: las regresiones son append-only, no se borran");
    }
}
