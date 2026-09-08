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
class AnalizadorLexicoOperadoresTest extends BaseDeAnalisisLexico {

    // Operadores y signos de puntuación, incluidos los que comparten prefijo.

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "(|PAREN_A", ")|PAREN_C", "{|LLAVE_A", "}|LLAVE_C",
            "[|CORCHETE_A", "]|CORCHETE_C", ";|PUNTO_Y_COMA",
            ",|COMA", ".|PUNTO", ":|DOS_PUNTOS",
    })
    void reconocePuntuacionDeUnCaracter(String simbolo, String tipoEsperado, @TempDir Path dir)
            throws Exception {
        Token token = crearAnalizador(dir, simbolo).proximoToken();

        assertEquals(TipoDeToken.valueOf(tipoEsperado), token.tipo());
        assertEquals(simbolo, token.lexema());
        assertEquals(1, token.linea());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            ">;OP_MAYOR", ">=;OP_MAYOR_IGUAL",
            "<;OP_MENOR", "<=;OP_MENOR_IGUAL",
            "!;OP_NEGACION", "!=;OP_DISTINTO",
            "=;OP_ASIGNACION", "==;OP_IGUAL",
            "+;OP_SUMA", "++;OP_INCREMENTO",
            "-;OP_RESTA", "--;OP_DECREMENTO",
            "&&;OP_AND", "||;OP_OR", "%;OP_MODULO",
            "*;OP_MULT", "/;OP_DIV",
    })
    void reconoceOperadores(String fuente, String tipoEsperado, @TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.valueOf(tipoEsperado), token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "(|PAREN_A", ")|PAREN_C", "{|LLAVE_A", "}|LLAVE_C",
            "[|CORCHETE_A", "]|CORCHETE_C", ";|PUNTO_Y_COMA",
            ",|COMA", ".|PUNTO", ":|DOS_PUNTOS",
    })
    void reconocePuntuacion(String fuente, String tipoEsperado, @TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.valueOf(tipoEsperado), token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void mayorSeguidoDeOtroCaracterNoConsumeElSiguiente(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, ">5");

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();

        assertEquals(TipoDeToken.OP_MAYOR, primero.tipo());
        assertEquals(TipoDeToken.INT_LITERAL, segundo.tipo());
        assertEquals("5", segundo.lexema());
    }

    @Test
    void unSoloAmpersandEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "&x");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("&", excepcion.getLexemaOfensivo());
        // Al '&' mismo, no al carácter que viene después: ése no tiene nada que ver.
        assertEquals(1, excepcion.getColumna());
    }

    @Test
    void unaSolaBarraVerticalEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "|x");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("|", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getColumna());
    }

}
