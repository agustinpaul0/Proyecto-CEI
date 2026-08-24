package minijava.lexico;

import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalizadorLexicoTest {

    private AnalizadorLexico crearAnalizador(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }

    @Test
    void fuenteVacioDaEofEnLineaUno(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "").proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals("$", token.lexema());
        assertEquals(1, token.linea());
    }

    @Test
    void saltaEspaciosYTabsAntesDeEof(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "  \t \t").proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
    }

    @Test
    void saltaSaltosDeLineaYCuentaLaLineaCorrecta(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\n\n").proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(3, token.linea());
    }

    // Regresión: los 3 casos de EOF documentados en AGENTS.md §3, verificados
    // acá a nivel AnalizadorLexico (no sólo del manejador), porque un bug real
    // de conteo de línea apareció en esta capa durante el desarrollo de
    // literales string (ver commit del fix en ContextoLexico.consumirCaracterActual).
    @Test
    void eofEnLinea22ConVeintiunSaltosYNewlineFinal(@TempDir Path dir) throws Exception {
        String fuente = "x" + "\n".repeat(21);
        AnalizadorLexico analizador = crearAnalizador(dir, fuente);
        analizador.proximoToken();
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(22, token.linea());
    }

    @Test
    void eofEnLinea13ConDoceSaltosSinNewlineFinal(@TempDir Path dir) throws Exception {
        String fuente = "\n".repeat(12) + "x";
        AnalizadorLexico analizador = crearAnalizador(dir, fuente);
        analizador.proximoToken();
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(13, token.linea());
    }

    @Test
    void eofEnLinea13ConDoceSaltosYNewlineFinal(@TempDir Path dir) throws Exception {
        String fuente = "\n".repeat(12);
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(13, token.linea());
    }

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

    @Test
    void reconoceIdMvSimple(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "v1").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
    }

    @Test
    void idMvAceptaMayusculasDigitosYGuionBajo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "abC_1;").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("abC_1", token.lexema());
    }

    @Test
    void idMvDeUnaSolaLetra(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "x").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("x", token.lexema());
    }

    @Test
    void reconoceIdGenUnaSolaMayuscula(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "A").proximoToken();

        assertEquals(TipoDeToken.ID_GEN, token.tipo());
        assertEquals("A", token.lexema());
    }

    @Test
    void reconoceIdClaseDosCaracteres(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "Ab").proximoToken();

        assertEquals(TipoDeToken.ID_CLASE, token.tipo());
        assertEquals("Ab", token.lexema());
    }

    @Test
    void idClaseAceptaMayusculasMinusculasDigitosYGuionBajo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "AlphaBeta_1;").proximoToken();

        assertEquals(TipoDeToken.ID_CLASE, token.tipo());
        assertEquals("AlphaBeta_1", token.lexema());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "class|PR_CLASS", "extends|PR_EXTENDS", "interface|PR_INTERFACE",
            "implements|PR_IMPLEMENTS", "static|PR_STATIC", "boolean|PR_BOOLEAN",
            "char|PR_CHAR", "int|PR_INT", "void|PR_VOID", "public|PR_PUBLIC",
            "if|PR_IF", "else|PR_ELSE", "while|PR_WHILE", "return|PR_RETURN",
            "var|PR_VAR", "this|PR_THIS", "new|PR_NEW", "null|PR_NULL",
            "true|PR_TRUE", "false|PR_FALSE", "float|PR_FLOAT", "double|PR_FLOAT",
    })
    // float y double se agregan como palabra reservada por confirmación directa
    // del profesor (no figura en los PDFs de la cátedra) — AGENTS.md §5. También por
    // indicación del profesor, "double" tokeniza como PR_FLOAT (mismo tipo que "float":
    // se tratan como un único tipo de dato float, no dos).
    void reconoceLasVeintidosPalabrasClave(String fuente, String tipoEsperado, @TempDir Path dir)
            throws Exception {
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.valueOf(tipoEsperado), token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void unaPalabraQueEmpiezaComoPalabraClaveNoLaConfunde(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "ifx").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("ifx", token.lexema());
    }

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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("1234567890", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("&", excepcion.getLexemaOfensivo());
    }

    @Test
    void unaSolaBarraVerticalEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "|x");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("|", excepcion.getLexemaOfensivo());
    }

    @Test
    void comentarioDeLineaSeSalteaYSigueElResto(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "// esto es un comentario\nv1");

        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
        assertEquals(2, token.linea());
    }

    @Test
    void comentarioDeLineaHastaElEofSinNewline(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "// comentario sin cierre");

        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
    }

    @Test
    void comentarioMultilineaSeSalteaYSigueElResto(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/* comentario\nde varias\nlineas */v1");

        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
        assertEquals(3, token.linea());
    }

    @Test
    void comentarioMultilineaConAsteriscosInternosNoSeCierraDeMas(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/* a ** b */v1");

        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
    }

    @Test
    void comentarioMultilineaSinCerrarEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/* nunca cierra");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("/*", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
    }

    @Test
    void divisionSeguidaDeIdentificadorNoSeConfundeConComentario(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/v1");

        Token primero = analizador.proximoToken();

        assertEquals(TipoDeToken.OP_DIV, primero.tipo());
        assertEquals("/", primero.lexema());
    }

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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    @Test
    void charLiteralVacioEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "''");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
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

    @Test
    void barraInvertidaEscapadaNoCierraElStringDeMas(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "\"a\\\\\"").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"a\\\\\"", token.lexema());
    }

    @Test
    void stringLiteralInterrumpidoPorSaltoDeLineaEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"hola\nchau\"");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(1, excepcion.getLinea());
    }

    @Test
    void stringLiteralSinCerrarHastaEofEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"sin cerrar");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
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

    @Test
    void exponenteSinDigitosEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "1e");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("1e", excepcion.getLexemaOfensivo());
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

    // Logro Multi-detección de Errores: tras un error, recuperarDeError()
    // debe permitir seguir escaneando SIN repetir el mismo error indefinidamente
    // (invariante de terminación, AGENTS.md §6).
    @Test
    void recuperarDeErrorPermiteSeguirEscaneandoTrasUnErrorLexico(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "#v1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
        analizador.recuperarDeError();
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
    }

    @Test
    void recuperarDeErrorEnElUltimoCaracterLlegaAEofSinExplotar(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "#");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
        analizador.recuperarDeError();
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
    }

    // Logro Unicodes: caracteres no-ASCII crudos dentro de literales char/string,
    // y la secuencia escapada de 4 hex dígitos (backslash + u + hex*4).
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

    // NOTA: se concatena "\\" con "u" + hex por separado en vez de escribir
    // la secuencia de corrido — javac decodifica esa secuencia como escape
    // Unicode DEL PROPIO ARCHIVO FUENTE antes de tokenizar strings.
    @Test
    void charLiteralAceptaEscapeUnicodeDeCuatroHexDigitos(@TempDir Path dir) throws Exception {
        String fuente = "'\\" + "u0041'";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.CHAR_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    @Test
    void escapeUnicodeConCaracterNoHexEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\\" + "u00G1'");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
    }

    // El JLS (§3.3) permite repetir el marcador 'u' del escape Unicode
    // (\uu0041, \uuu0041, ...) — semántica heredada tal cual en MiniJava
    // ante la falta de especificación propia de la cátedra (AGENTS.md §5).
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
