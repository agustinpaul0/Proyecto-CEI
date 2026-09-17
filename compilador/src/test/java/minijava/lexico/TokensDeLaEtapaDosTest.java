package minijava.lexico;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los cuatro tokens que NO figuran en «Aspectos Léxicos de MiniJava» y que el
// compilador reconoce porque los necesitan los logros de la Etapa 2: `for`,
// `private`, `?` y `->`.
@Tag("lexico")
class TokensDeLaEtapaDosTest {

    private AnalizadorLexico analizador(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }

    private List<Token> tokenizar(Path dir, String fuente) throws IOException, ExcepcionLexica {
        AnalizadorLexico analizador = analizador(dir, fuente);
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = analizador.proximoToken();
            tokens.add(token);
        } while (token.tipo() != TipoDeToken.EOF);
        return tokens;
    }

    // ---------------------------------------------------------------- for ---

    @Test
    void laPalabraForEsReservada(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "for")
                .getFirst();

        assertEquals(TipoDeToken.PR_FOR, token.tipo());
        assertEquals("for", token.lexema());
    }

    // ------------------------------------------------------------ private ---

    @Test
    void laPalabraPrivateEsReservada(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "private")
                .getFirst();

        assertEquals(TipoDeToken.PR_PRIVATE, token.tipo());
        assertEquals("private", token.lexema());
    }

    // Prefijos y sufijos: `format` no es `for`, ni con el logro prendido.
    @ParameterizedTest
    @ValueSource(strings = {"format", "forma", "for1", "for_", "privateX", "myprivate", "For", "FOR"})
    void unIdentificadorQueSoloEmpiezaOTerminaComoLaPalabraSigueSiendoUnIdentificador(
            String fuente) throws Exception {
        Path dir = Files.createTempDirectory("mj");
        Token token = tokenizar(dir, fuente).getFirst();

        assertTrue(token.tipo() == TipoDeToken.ID_MV || token.tipo() == TipoDeToken.ID_CLASE,
                fuente + " -> " + token.tipo());
        assertEquals(fuente, token.lexema());
    }

    // ------------------------------------------------------------------ ? ---

    @Test
    void elSignoDePreguntaEsUnToken(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "?")
                .getFirst();

        assertEquals(TipoDeToken.SIGNO_PREGUNTA, token.tipo());
        assertEquals("?", token.lexema());
    }

    @Test
    void elTernarioCompletoSeTokenizaConSuSignoDePreguntaYSusDosPuntos(@TempDir Path dir)
            throws Exception {
        List<Token> tokens = tokenizar(dir, "a ? b : c");

        assertEquals(List.of(TipoDeToken.ID_MV, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.ID_MV,
                        TipoDeToken.DOS_PUNTOS, TipoDeToken.ID_MV, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // ----------------------------------------------------------------- -> ---

    @Test
    void laFlechaEsUnSoloToken(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "->")
                .getFirst();

        assertEquals(TipoDeToken.OP_FLECHA, token.tipo());
        assertEquals("->", token.lexema());
    }

    // Maximal munch: con el logro prendido `-` sigue compitiendo con `--` y `->`,
    // y gana el más largo que exista. `-->` es `--` seguido de `>`, no `-` y `->`.
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "-    | op-",
            "--   | op_decremento",
            "->   | op_flecha",
            "-=   | op-",
            "---  | op_decremento",
    })
    void elMenosSigueEligiendoElLexemaMasLargoQueExiste(String fuente, String nombreEsperado)
            throws Exception {
        Path dir = Files.createTempDirectory("mj");
        Token token = tokenizar(dir, fuente).getFirst();

        assertEquals(nombreEsperado, token.tipo().getNombre());
    }

    @Test
    void laFlechaPegadaAOtrosTokensSeSeparaBien(@TempDir Path dir) throws Exception {
        List<Token> tokens = tokenizar(dir, "(x)->x+1");

        assertEquals(List.of(TipoDeToken.PAREN_A, TipoDeToken.ID_MV, TipoDeToken.PAREN_C,
                        TipoDeToken.OP_FLECHA, TipoDeToken.ID_MV, TipoDeToken.OP_SUMA,
                        TipoDeToken.INT_LITERAL, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // El '>' de la flecha no puede robarle el sufijo a `>=`: son estados distintos.
    @Test
    void elSufijoDeLaFlechaNoAfectaAlOperadorMayorIgual(@TempDir Path dir) throws Exception {
        List<Token> tokens = tokenizar(dir, "a >= b");

        assertEquals(TipoDeToken.OP_MAYOR_IGUAL, tokens.get(1).tipo());
    }

    // ------------------------------------------------------- combinaciones ---

    // Los cuatro conviven sin pisarse en un mismo fuente.
    @Test
    void losCuatroTokensNuevosConvivenEnUnMismoFuente(@TempDir Path dir) throws Exception {
        List<Token> tokens = tokenizar(dir, "private for ? ->");

        assertEquals(List.of(TipoDeToken.PR_PRIVATE, TipoDeToken.PR_FOR,
                        TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.OP_FLECHA, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // ----------------------------------------------------------- la tabla ---

}
