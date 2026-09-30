package minijava.casos;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El corpus de la Etapa 3 con un criterio más duro que el de la cátedra: no
// alcanza con que la salida CONTENGA el código declarado. El primer error
// semántico corta el análisis, así que la salida es exactamente un reporte.
@Tag("semantico")
class TesterDeCasosSemanticosEstricto {

    private static final Path SIN_ERRORES = Path.of("resources", "sinErrores");
    private static final Path CON_ERRORES = Path.of("resources", "conErrores");
    private static final String PREFIJO_DEL_MENSAJE = "Error Semantico en linea ";

    static Stream<Path> casosCorrectos() throws IOException {
        return casosDe(SIN_ERRORES);
    }

    static Stream<Path> casosConErrores() throws IOException {
        return casosDe(CON_ERRORES);
    }

    private static Stream<Path> casosDe(Path directorio) throws IOException {
        try (var archivos = Files.list(directorio)) {
            return archivos.filter(Files::isRegularFile)
                    .sorted(Comparator.naturalOrder()).toList().stream();
        }
    }

    @AfterAll
    static void informarElCorpusProcesado() {
        VolcadoDeCasos.resumirDirectorio(SIN_ERRORES.toString());
        VolcadoDeCasos.resumirDirectorio(CON_ERRORES.toString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosCorrectos")
    void unCasoCorrectoImprimeExactamenteElContratoDeExito(Path caso) throws IOException {
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"), ejecutar(caso),
                caso + ": la salida de un fuente valido es la de Pautas slide 31");
    }

    // Tres renglones —mensaje, blanco, código— y el mensaje nombra la misma línea
    // que el código (Pautas slides 28-29).
    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConErrores")
    void unCasoConErroresDaUnSoloReporteConElCodigoQueDeclara(Path caso) throws IOException {
        List<String> salida = ejecutar(caso);
        String codigo = codigoDeclarado(caso);

        assertEquals(3, salida.size(), caso + ": " + salida);
        assertEquals(codigo, salida.get(2), caso + ": " + salida);
        assertEquals("", salida.get(1), caso + ": falta el renglon en blanco");
        String linea = codigo.substring(codigo.lastIndexOf('|') + 1, codigo.length() - 1);
        assertTrue(salida.getFirst().startsWith(PREFIJO_DEL_MENSAJE + linea + ": "),
                caso + ": el mensaje no es semantico o no nombra la linea del codigo -> " + salida);
    }

    private String codigoDeclarado(Path caso) throws IOException {
        return Files.readAllLines(caso, StandardCharsets.UTF_8).getFirst().replaceFirst("^/+", "");
    }

    private List<String> ejecutar(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSemantico(salida).ejecutar(caso.toString());
        List<String> lineas = salida.getLineas();
        VolcadoDeCasos.volcar(caso.toString(), lineas);
        return lineas;
    }
}
