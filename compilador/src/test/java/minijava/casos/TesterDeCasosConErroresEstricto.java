package minijava.casos;

import minijava.aplicacion.ModuloPrincipalLexico;
import minijava.logros.ConfiguracionDeLaCorrida;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los MISMOS archivos que catedra.TesterDeCasosConErrores, que sólo verifica el
// primer código de error y ningún token entre medio. Cabecera de un caso:
//
//   // [Error:Lexema|NroLinea]     un código de error esperado, en orden
//   //#lexema,nroLinea             un token esperado, en orden
//   // [Completo]                  exige de acá en más la secuencia EXACTA
//
// La línea 1 siempre un `// [Error:...]`: el tester de la cátedra le hace substring(3).
@Tag("lexico")
class TesterDeCasosConErroresEstricto {

    private static final Path DIRECTORIO = Path.of("resources", "lexico", "conErrores");
    private static final String MARCA_ESTRICTA = "[Completo]";

    static Stream<Path> casos() throws IOException {
        try (var archivos = Files.list(DIRECTORIO)) {
            return archivos.filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList().stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void elCasoProduceExactamenteLosErroresYTokensDeclarados(Path caso) throws IOException {
        var requisitos = RequisitosDeLogrosDelCaso.de(caso);
        var configuracion = ConfiguracionDeLaCorrida.actual();
        assumeTrue(requisitos.seCumplenEn(configuracion),
                () -> caso + ": no aplica a esta configuracion de logros ("
                        + requisitos.describirIncumplidos(configuracion) + ")");

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

    // Todos y en orden, aunque el caso no sea estricto y haya otros entre medio.
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
        new ModuloPrincipalLexico(salida, ConfiguracionDeLaCorrida.actual()).ejecutar(caso.toString());
        List<String> lineas = salida.getLineas();
        VolcadoDeCasos.volcar(caso.toString(), lineas);
        return lineas;
    }

    @AfterAll
    static void informarElCorpusProcesado() {
        VolcadoDeCasos.resumirDirectorio(DIRECTORIO.toString());
    }

    // Los códigos van literales (son el contrato); los tokens se normalizan a
    // (lexema,linea) porque el nombre interno no es contrato. El criterio de
    // primera-coma/última-coma es el de la cátedra y admite lexemas con comas.
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
            // Los dos casos de la cátedra usan tres barras (///[Error:#|5]).
            String contenido = texto.substring(2).strip();
            if (contenido.startsWith("/")) {
                contenido = contenido.substring(1).strip();
            }
            if (contenido.equals(MARCA_ESTRICTA)) {
                // Lo anterior era la línea del tester de la cátedra: se descarta para
                // no contar dos veces el primer error.
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

    // //#lexema,linea contra (NombreToken,Lexema,NroLinea), ignorando el nombre.
    private record Cabecera(List<String> esperado, boolean estricta) {
    }
}
