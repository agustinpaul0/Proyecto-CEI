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

// El criterio de recuperación léxica, en una línea: TODO estado que lanza un
// error consume del fuente exactamente los caracteres que reporta en el
// lexema, ni uno más. No hay descarte posterior — el siguiente
// proximoToken() arranca en el primer carácter que NO formó parte del lexema
// reportado.
//
// La cátedra no da ninguna bajada de línea sobre CÓMO recuperarse (su
// comportamiento base es "reportar y finalizar"; la recuperación existe sólo
// por el logro Multi-detección), así que aplica AGENTS.md §0.1: como en Java.
// El comportamiento de javac se verificó ejecutándolo, no de memoria; cada
// test cita la salida real obtenida.
class CriterioDeRecuperacionTest {

    private List<String> ejecutar(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipal(salida).ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // javac sobre "char d = 'a;":
    //   error: unclosed character literal
    //   char d = 'a;
    //            ^
    // ...y NADA más: el ';' sobrevive y la línea siguiente compila limpia.
    // Acá el lexema reportado es "'a", así que el ';' no puede desaparecer.
    @Test
    void unCharSinCerrarNoSeTragaElCaracterQueLoInterrumpio(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "'a;");

        assertTrue(lineas.contains("[Error:'a|1]"), lineas::toString);
        assertTrue(lineas.contains("(puntoYComa,;,1)"), lineas::toString);
    }

    // javac sobre "int c = 1234567890123 + 9;":
    //   error: integer number too large
    // ...y el "+ 9" sobrevive. El lexema reportado es la corrida de dígitos
    // completa; el operador que la terminó nunca formó parte de él.
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

    // El caso más dañino de todos: sin el criterio, "v1 &v2" imprimía
    // (intLiteral,2,1) — la 'v' desaparecía y el identificador se convertía
    // en un número.
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

    // javac sobre "char c = 'abbc';":
    //   error: unclosed character literal   (en la ' de apertura)
    //   error: unclosed character literal   (en la ' de cierre)
    //   error: not a statement              ('bbc' quedó como identificador)
    // Es decir: consume 'a, sigue en la primera b, tokeniza bbc, y la comilla
    // de cierre abre un char nuevo que muere en el ';'. Misma estructura acá.
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

    // Invariante de terminación que reemplaza al viejo "cada recuperación
    // avanza exactamente un carácter": el lexema de un error nunca es vacío,
    // así que cada error consumió al menos un carácter y el análisis avanza.
    @Test
    void ningunErrorReportaUnLexemaVacio(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "@ # ? ~ ^ $ _ ` 'a \"b &  |  1e 1234567890");

        assertTrue(lineas.stream().filter(l -> l.startsWith("[Error:"))
                .noneMatch(l -> l.startsWith("[Error:|")), lineas::toString);
    }

    // Ninguna línea de salida puede llevar un carácter de control crudo: el
    // contrato de la cátedra es una línea por elemento, y un '\n' embebido en
    // un lexema parte [Error:...|N] en dos.
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
