package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoNumerosTest extends BaseDeAnalisisLexico {

    // Literales enteros y de punto flotante, con y sin el logro de floats.

    @Test
    void reconoceUnEnteroDeUnDigito(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "5").proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, token.tipo());
        assertEquals("5", token.lexema());
    }

    @Test
    void reconoceUnEnteroDeVariosDigitos(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "543").proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, token.tipo());
        assertEquals("543", token.lexema());
    }

    @Test
    void reconoceUnEnteroDeExactamenteNueveDigitos(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "123456789").proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, token.tipo());
        assertEquals("123456789", token.lexema());
    }

    @Test
    void unEnteroDeDiezDigitosEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "1234567890");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("1234567890", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
        // Habla del literal entero, así que el '^' va al primer dígito, como javac
        // con "integer number too large". Nunca al carácter siguiente al lexema.
        assertEquals(1, excepcion.getColumna());
    }

    @Test
    void unEnteroSeguidoDeLetrasDaDosTokens(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "1abc");

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, primero.tipo());
        assertEquals("1", primero.lexema());
        assertEquals(TipoDeToken.ID_MV, segundo.tipo());
        assertEquals("abc", segundo.lexema());
    }

    @Test
    void reconoceUnFloatSimple(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "1.5").proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals("1.5", token.lexema());
    }

    @Test
    void reconoceUnFloatSinParteFraccionaria(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "1.").proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals("1.", token.lexema());
    }

    @Test
    void reconoceUnFloatConExponente(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "1e5").proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals("1e5", token.lexema());
    }

    @Test
    void reconoceUnFloatConExponenteDeVariosDigitos(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "1e123").proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals("1e123", token.lexema());
    }

    @Test
    void reconoceUnFloatConExponenteDeVariosDigitosYSufijo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "1e12f").proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals("1e12f", token.lexema());
    }

    @Test
    void reconoceUnFloatConExponenteYSignoNegativo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "1.5E-3").proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals("1.5E-3", token.lexema());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {"1f;1f", "1.5d;1.5d", "1D;1D", "1F;1F"})
    void reconoceUnFloatConSufijo(String fuente, String lexemaEsperado, @TempDir Path dir)
            throws Exception {
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals(lexemaEsperado, token.lexema());
    }

    @Test
    void unPuntoSinDigitoAntesNoEsFloatSinoPuntoMasEntero(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, ".5");

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();

        assertEquals(TipoDeToken.PUNTO, primero.tipo());
        assertEquals(TipoDeToken.INT_LITERAL, segundo.tipo());
        assertEquals("5", segundo.lexema());
    }

    // Los dos hablan de la construcción entera, así que el '^' va donde empieza
    // (criterio de javac). Eso hace imposible que quede colgando fuera de la línea.
    @Test
    void exponenteSinDigitosEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "1e");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("1e", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getColumna());
        assertEquals("1e", excepcion.getLineaFuente());
    }

    @Test
    void enteroSeguidoDePuntoYLetraDaFloatYLuegoIdentificador(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "1.5x");

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();

        assertEquals(TipoDeToken.FLOAT_LITERAL, primero.tipo());
        assertEquals("1.5", primero.lexema());
        assertEquals(TipoDeToken.ID_MV, segundo.tipo());
        assertEquals("x", segundo.lexema());
    }

}
