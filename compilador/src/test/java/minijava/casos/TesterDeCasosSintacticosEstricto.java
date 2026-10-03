package minijava.casos;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// El corpus del analizador sintáctico, con un criterio más duro que el de la
// cátedra: no alcanza con que la salida CONTENGA el código declarado, tiene que
// ser el PRIMER código que emite y no puede haber ninguno de más.
//
// «Primer» y no «único» porque con Modo Pánico el análisis sigue después del
// primer error y reporta los que arrastra. El primero es el que el caso declara.
@Tag("sintactico")
class TesterDeCasosSintacticosEstricto {

    private static final Path SIN_ERRORES = Path.of("resources", "sintactico", "sinErrores");
    private static final Path CON_ERRORES = Path.of("resources", "sintactico", "conErrores");
    private static final String MENSAJE_DE_EXITO = "Compilacion Exitosa";

    static Stream<Path> casosCorrectos() throws IOException {
        return Stream.concat(casosDe(SIN_ERRORES), casosDe(generados().resolve("sinErrores")));
    }

    static Stream<Path> casosConErrores() throws IOException {
        return Stream.concat(casosDe(CON_ERRORES), casosDe(generados().resolve("conErrores")));
    }

    private static Path generados() throws IOException {
        return CorpusEmpaquetado.extraido(CorpusEmpaquetado.SINTACTICO_GENERADO);
    }

    private static Stream<Path> casosDe(Path directorio) throws IOException {
        if (!Files.isDirectory(directorio)) {
            return Stream.empty();
        }
        try (var archivos = Files.list(directorio)) {
            return archivos.filter(Files::isRegularFile)
                    .filter(caso -> !caso.getFileName().toString().startsWith("."))
                    .sorted(Comparator.naturalOrder()).toList().stream();
        }
    }

    @AfterAll
    static void informarElCorpusProcesado() throws IOException {
        VolcadoDeCasos.resumirDirectorio(SIN_ERRORES.toString());
        VolcadoDeCasos.resumirDirectorio(CON_ERRORES.toString());
        VolcadoDeCasos.resumirDirectorio(generados().resolve("sinErrores").toString());
        VolcadoDeCasos.resumirDirectorio(generados().resolve("conErrores").toString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosCorrectos")
    void unCasoCorrectoImprimeExactamenteElContratoDeExito(Path caso) throws IOException {

        assertEquals(List.of(MENSAJE_DE_EXITO, "", "[SinErrores]"), ejecutar(caso),
                caso + ": la salida de un fuente valido es la de Pautas slide 27");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConErrores")
    void unCasoConErroresDaExactamenteElCodigoQueDeclara(Path caso) throws IOException {
        List<String> salida = ejecutar(caso);
        List<String> codigos = codigosDe(salida);

        assertFalse(codigos.isEmpty(), caso + ": no se emitio ningun codigo -> " + salida);
        assertEquals(codigoDeclarado(caso), codigos.getFirst(),
                caso + ": el primer codigo no es el declarado -> " + salida);
        assertFalse(salida.contains("[SinErrores]"),
                caso + ": un caso con errores nunca puede declarar exito");
        assertFalse(salida.contains(MENSAJE_DE_EXITO),
                caso + ": un caso con errores nunca puede anunciar compilacion exitosa");
    }

    // Cada reporte son tres renglones —mensaje, blanco, código— y el mensaje
    // nombra la misma línea que el código. Vale igual con uno o con varios.
    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConErrores")
    void cadaReporteDeErrorTieneLaFormaDePautas(Path caso) throws IOException {
        List<String> salida = ejecutar(caso);

        assertEquals(0, salida.size() % 3, caso + ": " + salida);
        for (int i = 0; i < salida.size(); i += 3) {
            String mensaje = salida.get(i);
            String codigo = salida.get(i + 2);
            assertTrue(mensaje.startsWith("Error Sintactico en linea "), caso + ": " + mensaje);
            assertEquals("", salida.get(i + 1), caso + ": falta el renglon en blanco");
            String linea = codigo.substring(codigo.lastIndexOf('|') + 1, codigo.length() - 1);
            assertTrue(mensaje.startsWith("Error Sintactico en linea " + linea + ":"),
                    caso + ": el mensaje y el codigo no coinciden en la linea -> " + mensaje
                            + " / " + codigo);
        }
    }

    private List<String> codigosDe(List<String> salida) {
        return salida.stream().filter(linea -> linea.startsWith("[")).toList();
    }

    // La expectativa va en la primera línea, con tres barras: es el formato de
    // los casos que la cátedra adjunta en testersSintactico.
    private String codigoDeclarado(Path caso) throws IOException {
        return Files.readAllLines(caso, StandardCharsets.UTF_8).getFirst().replaceFirst("^/+", "");
    }

    private List<String> ejecutar(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida).ejecutar(caso.toString());
        List<String> lineas = salida.getLineas();
        VolcadoDeCasos.volcar(caso.toString(), lineas);
        return lineas;
    }
}
