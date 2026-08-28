package minijava.lexico;

import minijava.config.ConfiguracionDelCompilador;
import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalizadorLexicoTest {

    private AnalizadorLexico crearAnalizador(Path dir, String fuente) throws IOException {
        return crearAnalizador(dir, fuente, ConfiguracionDelCompilador.DEFAULT);
    }

    private AnalizadorLexico crearAnalizador(Path dir, String fuente, ConfiguracionDelCompilador configuracion)
            throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador, configuracion);
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

    // Los 3 casos de EOF de agents/convenciones-catedra.md, verificados a nivel
    // analizador y no sólo del manejador: un bug de conteo apareció en esta capa.
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
            "true|PR_TRUE", "false|PR_FALSE", "float|PR_FLOAT",
    })
    // float es reservada por confirmación directa del profesor; "double" NO lo es
    // (los sufijos f/F/d/D del literal ya cubren ambos). agents/especificacion-lexica.md.
    void reconoceLasVeintiunaPalabrasClave(String fuente, String tipoEsperado, @TempDir Path dir)
            throws Exception {
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.valueOf(tipoEsperado), token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void dobleNoEsPalabraClaveEsUnIdentificadorComun(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "double").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("double", token.lexema());
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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("&", excepcion.getLexemaOfensivo());
        // Al '&' mismo, no al carácter que viene después: ése no tiene nada que ver.
        assertEquals(1, excepcion.getColumna());
    }

    @Test
    void unaSolaBarraVerticalEsError(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "|x");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("|", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getColumna());
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

    // Los tres campos (línea, columna, línea fuente) tienen que describir la
    // APERTURA a la vez, no el número correcto con la línea fuente del EOF.
    @Test
    void comentarioSinCerrarConVariasLineasEnBlancoAntesDelEofReportaLaLineaDeApertura(
            @TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "v1\n/* abre aca\n\n\n");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, () -> {
                    analizador.proximoToken();
                    analizador.proximoToken();
                });

        assertEquals(2, excepcion.getLinea());
        assertEquals("/* abre aca", excepcion.getLineaFuente());
        assertEquals(1, excepcion.getColumna());
    }

    // Un comentario de LINEA no puede estar mal formado: `//` se cierra con '\n' o
    // con el EOF y las dos terminaciones son validas -- EstadoComentarioDeLinea no
    // tiene ninguna rama de error. Lo que sigue fija esos bordes, que en el corpus
    // de resources/ no se pueden escribir: sus lineas de oraculo son comentarios y
    // quedarian despues del constructo que tiene que cerrar el archivo.
    @Test
    void comentarioDeLineaVacioQueEsTodoElArchivoDaEofEnLineaUno(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "//").proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(1, token.linea());
    }

    // El '\n' que cierra el comentario lo consume EstadoInicial como blanco: el EOF
    // cae en la linea siguiente, no en la del comentario.
    @Test
    void comentarioDeLineaVacioSeguidoDeSaltoDaEofEnLaLineaSiguiente(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "//\n").proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(2, token.linea());
    }

    @Test
    void comentarioDeLineaVacioDespuesDeUnTokenNoSeLoCome(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "a //");

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, primero.tipo());
        assertEquals("a", primero.lexema());
        assertEquals(TipoDeToken.EOF, segundo.tipo());
        assertEquals(1, segundo.linea());
    }

    // El centinela de EOF es un byte legitimo del fuente: adentro de un comentario
    // se descarta como cualquier otro y no trunca el analisis.
    @Test
    void elCentinelaDeEofDentroDeUnComentarioDeLineaNoTruncaElAnalisis(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "// \u001a\u0000 raro\nv1");

        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
        assertEquals(2, token.linea());
    }

    @Test
    void aperturaDeComentarioQueEsTodoElArchivoReportaLaColumnaUno(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/*");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("/*", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
        assertEquals(1, excepcion.getColumna());
    }

    @Test
    void elCentinelaDeEofDentroDeUnComentarioSinCerrarNoAdelantaElError(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/* \u001a sigue abierto\n\n");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("/*", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
    }

    @Test
    void barraSolaAlFinalDelArchivoEsUnaDivision(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "a /");

        analizador.proximoToken();
        Token segundo = analizador.proximoToken();
        Token tercero = analizador.proximoToken();

        assertEquals(TipoDeToken.OP_DIV, segundo.tipo());
        assertEquals("/", segundo.lexema());
        assertEquals(TipoDeToken.EOF, tercero.tipo());
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

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("''", excepcion.getLexemaOfensivo());
    }

    // El lexema no debe llevar el '\n' crudo: un control embebido parte en dos el
    // [Error:Lexema|Linea], que la cátedra espera en una sola línea.
    @Test
    void comillaDeAperturaSeguidaDeSaltoDeLineaNoIncluyeElSaltoEnElLexema(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\nv1");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'", excepcion.getLexemaOfensivo());
    }

    @Test
    void comillaDeAperturaSeguidaDeEofNoIncluyeElCentinelaEnElLexema(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("1e", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getColumna());
        assertEquals("1e", excepcion.getLineaFuente());
    }

    @Test
    void charLiteralInterrumpidoPorSaltoDeLineaApuntaALaComillaDeApertura(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'a\nv1");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'a", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getColumna());
        assertEquals("'a", excepcion.getLineaFuente());
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

    // Multi-detección: tras un error se sigue escaneando sin repetirlo para siempre,
    // porque el estado que lanzó ya consumió lo que reportó.
    @Test
    void trasUnErrorLexicoSePuedeSeguirEscaneando(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "#v1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
    }

    @Test
    void unErrorEnElUltimoCaracterLlegaAEofSinExplotar(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "#");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
    }

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

    // JLS §3.10.7: en un litString sólo valen b,s,t,n,f,r,",',\ y un octal 0-7.
    // La cátedra no le da a litString la permisividad que sí le da a charLiteral.
    @ParameterizedTest
    @ValueSource(strings = {"b", "s", "t", "n", "f", "r", "\"", "'", "\\", "0", "7"})
    void stringLiteralAceptaSoloLosEscapesValidosDeJava(String escape, @TempDir Path dir)
            throws Exception {
        String fuente = "\"antes \\" + escape + " despues\"";
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    // Fatal: se reporta el primero y se descarta el literal hasta su comilla de
    // cierre, sin emitir token. Criterio del profesor (decisiones.md §4.3).
    @ParameterizedTest
    @ValueSource(strings = {"q", "x", "d", "8", "9", "A"})
    void stringLiteralConEscapeInvalidoSegunJavaEsErrorFatal(String escape, @TempDir Path dir)
            throws Exception {
        String fuente = "\"antes \\" + escape + " despues\"";
        AnalizadorLexico analizador = crearAnalizador(dir, fuente);

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.errores.TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, excepcion.getTipo());
        assertEquals("\"antes \\" + escape, excepcion.getLexemaOfensivo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // Sólo el primero: el segundo es un defecto derivado del mismo literal roto.
    @Test
    void dosEscapesInvalidosEnUnStringReportanSoloElPrimero(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\q b\\z c\"");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("\"a\\q", excepcion.getLexemaOfensivo());
        assertEquals("\\q", excepcion.getFragmentoOfensivo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // El descarte para en la comilla de CIERRE, no se come el resto de la línea.
    @Test
    void trasElDescarteElAnalisisSigueEnElTokenPosteriorALaComillaDeCierre(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"x\\q\" + v1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

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
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\q\" \"b\\z\"");

        var primero = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);
        var segundo = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("\"a\\q", primero.getLexemaOfensivo());
        assertEquals("\"b\\z", segundo.getLexemaOfensivo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // Sin comilla de cierre, el descarte se detiene en el salto de línea.
    @Test
    void elDescarteNoSeComeLaLineaSiguienteCuandoFaltaLaComillaDeCierre(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\q\nv1");

        org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

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
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.errores.TipoDeErrorLexico.UNICODE_MAL_FORMADO, excepcion.getTipo());
        assertEquals("\"a\\" + "u12", excepcion.getLexemaOfensivo());
        assertEquals(TipoDeToken.OP_SUMA, analizador.proximoToken().tipo());
    }

    // Regresión del alcance: en un charLiteral no hay resincronización.
    @Test
    void escapeUnicodeMalFormadoEnUnCharLiteralConservaLaRecuperacionEstructural(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "'\\" + "u12G4'");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("'\\" + "u12", excepcion.getLexemaOfensivo());
        Token siguiente = analizador.proximoToken();
        assertEquals("G4", siguiente.lexema());
    }

    // Sin el logro Floats I, '.' y 'e'/'E' terminan el intLiteral.
    @Test
    void conFloatsDeshabilitadosElPuntoNoContinuaElEntero(@TempDir Path dir) throws Exception {
        var configuracion = new ConfiguracionDelCompilador(false, true, true, true);
        AnalizadorLexico analizador = crearAnalizador(dir, "1.5", configuracion);

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();
        Token tercero = analizador.proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, primero.tipo());
        assertEquals("1", primero.lexema());
        assertEquals(TipoDeToken.PUNTO, segundo.tipo());
        assertEquals(TipoDeToken.INT_LITERAL, tercero.tipo());
        assertEquals("5", tercero.lexema());
    }

    @Test
    void conFloatsDeshabilitadosLaEYaNoIniciaUnExponente(@TempDir Path dir) throws Exception {
        var configuracion = new ConfiguracionDelCompilador(false, true, true, true);
        AnalizadorLexico analizador = crearAnalizador(dir, "1e5", configuracion);

        Token primero = analizador.proximoToken();
        Token segundo = analizador.proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, primero.tipo());
        assertEquals("1", primero.lexema());
        assertEquals(TipoDeToken.ID_MV, segundo.tipo());
        assertEquals("e5", segundo.lexema());
    }

    // Sin el logro Unicodes la 'u' deja de ser marcador y pasa a ser un escape
    // inválido. Ojo: no escribir la secuencia literal acá, javac la decodifica
    // hasta en los comentarios (JLS §3.3) y falla sin 4 hex dígitos detrás.
    @Test
    void conUnicodeDeshabilitadoElEscapeDeUnicodeEnStringEsError(@TempDir Path dir) throws Exception {
        var configuracion = new ConfiguracionDelCompilador(true, false, true, true);
        AnalizadorLexico analizador = crearAnalizador(dir, "\"a\\u0041\"", configuracion);

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.errores.TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, excepcion.getTipo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }
}
