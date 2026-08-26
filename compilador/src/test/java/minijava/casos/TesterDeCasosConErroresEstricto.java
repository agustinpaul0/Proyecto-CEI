package minijava.casos;

import minijava.aplicacion.ModuloPrincipal;
import minijava.salida.SalidaEnMemoria;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Corre sobre los MISMOS archivos que catedra.TesterDeCasosConErrores, que se
// deja intacto al lado. Ese tester sólo hace containsString del PRIMER código
// de error declarado, así que un caso con cinco errores queda casi sin
// verificar, y nada comprueba qué tokens salen entre medio — que es
// justamente donde se escondía el defecto de la recuperación tragándose
// caracteres.
//
// Formato de la cabecera (las líneas de comentario al principio del archivo):
//
//   // [Error:Lexema|NroLinea]     un código de error esperado, en orden
//   //#lexema,nroLinea             un token esperado, en orden
//   // [Completo]                  activa la verificación estricta
//
// La línea 1 tiene que ser siempre un `// [Error:...]` para que el tester de
// la cátedra (que lee sólo esa línea y hace substring(3)) siga funcionando.
//
// Sin `[Completo]` se verifica que todos los códigos declarados aparezcan en
// orden — ya más fuerte que la cátedra, y retrocompatible con los casos
// viejos. Con `[Completo]` se exige además la secuencia EXACTA de errores y
// tokens, sin nada de más ni de menos.
class TesterDeCasosConErroresEstricto {

    private static final Path DIRECTORIO = Path.of("resources", "conErrores");
    private static final String MARCA_ESTRICTA = "[Completo]";

    static Stream<Path> casos() throws IOException {
        try (var archivos = Files.list(DIRECTORIO)) {
            return archivos.filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void elCasoProduceExactamenteLosErroresYTokensDeclarados(Path caso) throws IOException {
        Cabecera cabecera = leerCabecera(caso);
        List<String> salida = ejecutar(caso);
        List<String> obtenido = soloErroresYTokens(salida);

        assertFalse(cabecera.esperado.isEmpty(),
                () -> caso + ": la cabecera no declara ningún [Error:...]");
        assertFalse(salida.contains("[SinErrores]"),
                () -> caso + ": un caso de conErrores/ nunca puede informar analisis exitoso.\n"
                        + String.join("\n", salida));
        assertTrue(salida.stream().noneMatch(l -> l.indexOf('\n') >= 0 || l.indexOf('\r') >= 0),
                () -> caso + ": una linea de salida lleva un salto de linea embebido, lo que parte "
                        + "el formato de un elemento por linea.\n" + String.join("\n", salida));

        if (cabecera.estricta) {
            assertEquals(cabecera.esperado, obtenido,
                    () -> caso + ": la secuencia completa de errores y tokens no coincide.\n"
                            + "Salida completa:\n" + String.join("\n", salida));
        } else {
            verificarSubsecuenciaEnOrden(caso, cabecera.esperado, obtenido, salida);
        }
    }

    // Los códigos declarados tienen que aparecer todos y en el mismo orden,
    // aunque el caso no sea estricto y haya otros elementos entre medio.
    private void verificarSubsecuenciaEnOrden(Path caso, List<String> esperado, List<String> obtenido,
                                              List<String> salida) {
        int siguiente = 0;
        for (String elemento : obtenido) {
            if (siguiente < esperado.size() && elemento.equals(esperado.get(siguiente))) {
                siguiente++;
            }
        }
        int encontrados = siguiente;
        assertEquals(esperado.size(), encontrados,
                () -> caso + ": faltan elementos declarados o estan fuera de orden.\n"
                        + "Declarados: " + esperado + "\nObtenidos: " + obtenido
                        + "\nSalida completa:\n" + String.join("\n", salida));
    }

    private List<String> ejecutar(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipal(salida).ejecutar(caso.toString());
        return salida.getLineas();
    }

    // Los códigos de error se comparan literales (son el contrato de la
    // cátedra). Los tokens se normalizan a (lexema,linea) descartando el
    // nombre del token, con el mismo criterio de primera-coma/última-coma que
    // usa el tester de la cátedra: los nombres internos no son contrato, y
    // así se admiten lexemas que contienen comas.
    private List<String> soloErroresYTokens(List<String> salida) {
        List<String> elementos = new ArrayList<>();
        for (String linea : salida) {
            if (linea.startsWith("[Error:")) {
                elementos.add(linea);
            } else if (linea.startsWith("(") && linea.endsWith(")")) {
                elementos.add("(" + linea.substring(linea.indexOf(',') + 1));
            }
        }
        return List.copyOf(elementos);
    }

    private Cabecera leerCabecera(Path caso) throws IOException {
        List<String> esperado = new ArrayList<>();
        boolean estricta = false;

        for (String linea : Files.readAllLines(caso, StandardCharsets.UTF_8)) {
            String texto = linea.strip();
            if (!texto.startsWith("//")) {
                break;
            }
            // Los dos casos originales de la cátedra escriben la cabecera con
            // TRES barras y sin espacio (///[Error:#|5]), porque su tester
            // hace un substring(3) crudo. Se acepta esa forma también.
            String contenido = texto.substring(2).strip();
            if (contenido.startsWith("/")) {
                contenido = contenido.substring(1).strip();
            }
            if (contenido.equals(MARCA_ESTRICTA)) {
                // Todo lo anterior era la línea que lee el tester de la
                // cátedra (que sólo mira la primera): la secuencia exacta
                // empieza acá, así que se descarta lo acumulado para no
                // contar dos veces el primer error.
                estricta = true;
                esperado.clear();
            } else if (contenido.startsWith("[Error:")) {
                esperado.add(contenido);
            } else if (contenido.startsWith("#")) {
                esperado.add("(" + contenido.substring(1) + ")");
            }
        }
        return new Cabecera(List.copyOf(esperado), estricta);
    }

    // Un token declarado como //#lexema,linea se compara contra la salida
    // (NombreToken,Lexema,NroLinea) ignorando el nombre del token, igual que
    // hace el tester de la cátedra: los nombres internos no son contrato.
    private record Cabecera(List<String> esperado, boolean estricta) {
    }
}
