package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoUnicodeTest extends BaseDeAnalisisLexico {

    // Logro Unicodes: no-ASCII crudos en literales, y el escape de 4 hex dígitos.
    @Test
    void stringLiteralAceptaCaracteresUnicodeCrudos(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"ñoño 中文 café\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"ñoño 中文 café\"", token.lexema());
    }

    @Test
    void charLiteralAceptaUnCaracterUnicodeCrudo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "'ñ'").proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals("'ñ'", token.lexema());
    }

    // Se concatena en vez de escribir la secuencia de corrido: javac la decodificaría
    // como escape Unicode de ESTE archivo antes de tokenizar (JLS §3.3).
    @Test
    void charLiteralAceptaEscapeUnicodeDeCuatroHexDigitos(@TempDir Path dir) throws Exception {
        String fuente = "'\\" + "u0041'";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void escapeUnicode20ccConsumeSusCuatroDigitosComoUnSoloLiteral(@TempDir Path dir)
            throws Exception {
        String fuente = "'\\" + "u20cc'";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void escapeUnicodeMalFormadoNoConsumeElPrimerCaracterNoHexadecimal(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\\" + "u20cG'");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'\\" + "u20c", excepcion.getLexemaOfensivo());
        assertEquals(7, excepcion.getColumna());
    }

    @Test
    void stringLiteralAceptaEscapeUnicodeEmbebido(@TempDir Path dir) throws Exception {
        String fuente = "\"letra \\" + "u00F1 final\"";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void escapeUnicodeConMenosDeCuatroHexDigitosEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\\" + "u04'");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    @Test
    void escapeUnicodeConCaracterNoHexEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\\" + "u00G1'");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    // JLS §3.3 permite repetir el marcador 'u'; heredado tal cual ante la falta de
    // especificación propia de la cátedra.
    @Test
    void charLiteralAceptaEscapeUnicodeConMultiplesUEsValido(@TempDir Path dir) throws Exception {
        String fuente = "'\\" + "uu0041'";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void stringLiteralAceptaEscapeUnicodeConMultiplesUEsValido(@TempDir Path dir) throws Exception {
        String fuente = "\"letra \\" + "uuuu00F1 final\"";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals(fuente, token.lexema());
    }

}
