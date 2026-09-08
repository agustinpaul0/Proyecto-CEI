package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoLiteralesTest extends BaseDeAnalisisLexico {

    // Literales de carácter y de string, cerrados y sin cerrar.

    @Test
    void reconoceUnCharLiteralSimple(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "'a'").proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals("'a'", token.lexema());
    }

    @Test
    void reconoceUnCharLiteralEscapado(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "'\\n'").proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals("'\\n'", token.lexema());
    }

    @Test
    void charLiteralEscapaCualquierCaracterArbitrario(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "'\\q'").proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals("'\\q'", token.lexema());
    }

    @Test
    void charLiteralPuedeEscaparLaComillaSimple(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "'\\''").proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals("'\\''", token.lexema());
    }

    @Test
    void charLiteralSinCerrarEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'a");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    @Test
    void charLiteralVacioEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "''");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("''", excepcion.getLexemaOfensivo());
    }

    // El lexema no debe llevar el '\n' crudo: un control embebido parte en dos el
    // [Error:Lexema|Linea], que la cátedra espera en una sola línea.
    @Test
    void comillaDeAperturaSeguidaDeSaltoDeLineaNoIncluyeElSaltoEnElLexema(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\nv1");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'", excepcion.getLexemaOfensivo());
    }

    @Test
    void comillaDeAperturaSeguidaDeEofNoIncluyeElCentinelaEnElLexema(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'", excepcion.getLexemaOfensivo());
    }

    @Test
    void reconoceUnStringLiteralVacio(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"\"", token.lexema());
    }

    @Test
    void reconoceUnStringLiteralConTexto(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"hola, mundo\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"hola, mundo\"", token.lexema());
    }

    @Test
    void stringLiteralPuedeEscaparLaComillaDoble(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"di \\\"hola\\\"\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"di \\\"hola\\\"\"", token.lexema());
    }

    // Aspectos Léxicos §2.3.3 es una regla POSICIONAL: la comilla que cierra
    // nunca está antecedida por una barra. Una barra tras otra barra no "se
    // cancela": la comilla que sigue igual queda antecedida por barra.
    @Test
    void unaBarraAntesDeLaComillaDeCierreDejaElStringSinCerrar(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\\\\"");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.lexico.errores.TipoDeErrorLexico.STRING_SIN_CERRAR, excepcion.getTipo());
        assertEquals("\"a\\\\\"", excepcion.getLexemaOfensivo());
    }

    // Tres barras: la tercera vuelve a dejar antecedida a la comilla siguiente,
    // que por eso no cierra; cierra recién la que va después.
    @Test
    void tresBarrasYDosComillasCierranEnLaSegundaComilla(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"a\\\\\\\"\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"a\\\\\\\"\"", token.lexema());
    }

    // La barra sólo importa pegada a la comilla de cierre: en el medio es un
    // carácter común y el literal cierra normalmente.
    @Test
    void unaBarraEnElMedioNoImpideElCierre(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"barra \\\\ al medio\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"barra \\\\ al medio\"", token.lexema());
    }

    @Test
    void unStringTerminadoEnComillaEscapadaQuedaSinCerrar(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"abc\\\"");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.lexico.errores.TipoDeErrorLexico.STRING_SIN_CERRAR, excepcion.getTipo());
    }

    @Test
    void stringLiteralInterrumpidoPorSaltoDeLineaEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"hola\nchau\"");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(1, excepcion.getLinea());
    }

    @Test
    void stringLiteralSinCerrarHastaEofEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"sin cerrar");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    @Test
    void charLiteralInterrumpidoPorSaltoDeLineaApuntaALaComillaDeApertura(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'a\nv1");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'a", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getColumna());
        assertEquals("'a", excepcion.getLineaFuente());
    }

    // No hay lista de escapes válidos: §2.3.3 no da una producción con barra,
    // sólo dice que una comilla antecedida por barra no cierra. Así que tras una
    // barra vale cualquier carácter, igual que en charLiteral (§2.3.2).
    @ParameterizedTest
    @ValueSource(strings = {"b", "s", "t", "n", "f", "r", "\"", "'", "0", "7",
            "q", "x", "d", "8", "9", "A", "-", " "})
    void stringLiteralAceptaCualquierCaracterTrasLaBarra(String escape, @TempDir Path dir)
            throws Exception {
        String fuente = "\"antes \\" + escape + " despues\"";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    // Varias barras sueltas en un mismo literal: ninguna es un error, el
    // literal es uno solo y su lexema va crudo.
    @Test
    void variasBarrasSueltasEnUnStringNoSonError(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"a\\q b\\z c\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"a\\q b\\z c\"", token.lexema());
    }

}
