package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoRecuperacionTest extends BaseDeAnalisisLexico {

    // Qué descarta el analizador tras un error y dónde retoma.

    // Multi-detección: tras un error se sigue escaneando sin repetirlo para siempre,
    // porque el estado que lanzó ya consumió lo que reportó.
    @Test
    void trasUnErrorLexicoSePuedeSeguirEscaneando(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "#v1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
    }

    @Test
    void unErrorEnElUltimoCaracterLlegaAEofSinExplotar(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "#");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
    }

    // El descarte para en la comilla de CIERRE, no se come el resto de la línea.
    // El único error que lo dispara es el escape Unicode mal formado.
    @Test
    void trasElDescarteElAnalisisSigueEnElTokenPosteriorALaComillaDeCierre(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"x\\" + "u12G4\" + v1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(TipoDeToken.OP_SUMA, analizador.proximoToken().tipo());
        Token identificador = analizador.proximoToken();
        assertEquals(TipoDeToken.ID_MV, identificador.tipo());
        assertEquals("v1", identificador.lexema());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // La multi-detección sigue viva: lo único que se pierde es un segundo error
    // DENTRO del mismo literal.
    @Test
    void unSegundoLitStringRotoEnLaMismaLineaReportaSuPropioError(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir,
                "\"a\\" + "u12G4\" \"b\\" + "u34Z6\"");

        var primero = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);
        var segundo = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("\"a\\" + "u12", primero.getLexemaOfensivo());
        assertEquals("\"b\\" + "u34", segundo.getLexemaOfensivo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // Sin comilla de cierre, el descarte se detiene en el salto de línea.
    @Test
    void elDescarteNoSeComeLaLineaSiguienteCuandoFaltaLaComillaDeCierre(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\" + "u12G4\nv1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        Token identificador = analizador.proximoToken();
        assertEquals(TipoDeToken.ID_MV, identificador.tipo());
        assertEquals("v1", identificador.lexema());
        assertEquals(2, identificador.linea());
    }

    // Misma regla para el Unicode mal formado: un solo criterio, no dos.
    @Test
    void escapeUnicodeMalFormadoEnUnStringDescartaHastaLaComillaDeCierre(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\" + "u12G4\" + v1");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.lexico.errores.TipoDeErrorLexico.UNICODE_MAL_FORMADO, excepcion.getTipo());
        assertEquals("\"a\\" + "u12", excepcion.getLexemaOfensivo());
        assertEquals(TipoDeToken.OP_SUMA, analizador.proximoToken().tipo());
    }

    // Regresión del alcance: en un charLiteral no hay resincronización.
    @Test
    void escapeUnicodeMalFormadoEnUnCharLiteralConservaLaRecuperacionEstructural(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\\" + "u12G4'");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'\\" + "u12", excepcion.getLexemaOfensivo());
        Token siguiente = analizador.proximoToken();
        assertEquals("G4", siguiente.lexema());
    }

}
