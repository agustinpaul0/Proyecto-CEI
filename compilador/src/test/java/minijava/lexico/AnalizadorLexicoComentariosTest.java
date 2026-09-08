package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoComentariosTest extends BaseDeAnalisisLexico {

    // Comentarios de línea y multilínea, y la división que comparte el prefijo.

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
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

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
                minijava.lexico.errores.ExcepcionLexica.class, () -> {
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
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

        assertEquals("/*", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
        assertEquals(1, excepcion.getColumna());
    }

    @Test
    void elCentinelaDeEofDentroDeUnComentarioSinCerrarNoAdelantaElError(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crearAnalizador(dir, "/* \u001a sigue abierto\n\n");

        var excepcion = org.junit.jupiter.api.Assertions.assertThrows(
                minijava.lexico.errores.ExcepcionLexica.class, analizador::proximoToken);

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

}
