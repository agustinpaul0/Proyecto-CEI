package minijava.lexico;

import minijava.aplicacion.ModuloPrincipal;
import minijava.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El criterio de recuperación: todo estado que lanza un error consume exactamente
// lo que reporta en el lexema, ni uno más. La cátedra no dice cómo recuperarse, así
// que aplica AGENTS.md §0.1; cada test cita la salida real de javac.
class CriterioDeRecuperacionTest {

    private List<String> ejecutar(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipal(salida).ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // javac sobre "char d = 'a;": un unclosed character literal y nada más — el
    // ';' sobrevive. Acá el lexema es "'a", así que tampoco puede desaparecer.
    @Test
    void unCharSinCerrarNoSeTragaElCaracterQueLoInterrumpio(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "'a;");

        assertTrue(lineas.contains("[Error:'a|1]"), lineas::toString);
        assertTrue(lineas.contains("(puntoYComa,;,1)"), lineas::toString);
    }

    // javac sobre "int c = 1234567890123 + 9;": integer number too large, y el
    // "+ 9" sobrevive — el operador nunca formó parte del lexema.
    @ParameterizedTest
    @CsvSource({
            "'1234567890+1', '[Error:1234567890|1]', '(op+,+,1)'",
            "'1234567890;',  '[Error:1234567890|1]', '(puntoYComa,;,1)'",
            "'1e;',          '[Error:1e|1]',         '(puntoYComa,;,1)'",
            "'1.5e+;',       '[Error:1.5e+|1]',      '(puntoYComa,;,1)'"
    })
    void unLexemaYaCompletoNoSeTragaElLookahead(String fuente, String codigo, String token,
                                                @TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, fuente);

        assertTrue(lineas.contains(codigo), lineas::toString);
        assertTrue(lineas.contains(token), lineas::toString);
    }

    // El más dañino: sin el criterio, "v1 &v2" imprimía (intLiteral,2,1) — la 'v'
    // desaparecía y el identificador se volvía un número.
    @Test
    void unOperadorDobleIncompletoNoSeTragaElIdentificadorSiguiente(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "v1 &v2");

        assertTrue(lineas.contains("[Error:&|1]"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
    }

    @Test
    void unPipeSueltoSeComportaIgualQueElAmpersand(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "v1 |v2");

        assertTrue(lineas.contains("[Error:||1]"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
    }

    // javac sobre "char c = 'abbc';": dos unclosed character literal y un not a
    // statement — consume 'a, tokeniza bbc, y la ' de cierre abre otro char.
    @Test
    void unCharConVariosCaracteresReproduceLaEstructuraDeJavac(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "'abbc'");

        assertEquals(List.of(
                "[Error:'a|1]",
                "(idMV,bbc,1)",
                "[Error:'|1]",
                "(EOF,$,1)"
        ), lineas.stream().filter(l -> l.startsWith("[Error:") || l.startsWith("(")).toList());
    }

    @Test
    void unCharConDosCaracteresNoSeTragaElDelMedio(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "'ab'");

        assertTrue(lineas.contains("[Error:'a|1]"), lineas::toString);
        assertTrue(lineas.contains("(idMV,b,1)"), lineas::toString);
        assertTrue(lineas.contains("[Error:'|1]"), lineas::toString);
    }

    // Terminación: el lexema de un error nunca es vacío, así que siempre se avanzó
    // al menos un carácter.
    @Test
    void ningunErrorReportaUnLexemaVacio(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "@ # ? ~ ^ $ _ ` 'a \"b &  |  1e 1234567890");

        assertTrue(lineas.stream().filter(l -> l.startsWith("[Error:"))
                .noneMatch(l -> l.startsWith("[Error:|")), lineas::toString);
    }

    // Un '\n' embebido en un lexema parte [Error:...|N] en dos, y la cátedra espera
    // una línea por elemento.
    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "'\\\n'",
            "\"abc\\\n\"",
            "'\\",
            "\"abc\\"
    })
    void ningunaLineaDeSalidaLlevaUnCaracterDeControlCrudo(String fuente, @TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, fuente);

        assertTrue(lineas.stream().noneMatch(l -> l.indexOf('\n') >= 0 || l.indexOf('\r') >= 0),
                lineas::toString);
        assertTrue(lineas.stream().filter(l -> l.startsWith("[Error:"))
                        .noneMatch(l -> l.chars().anyMatch(c -> c < 0x20)),
                lineas::toString);
    }
}
