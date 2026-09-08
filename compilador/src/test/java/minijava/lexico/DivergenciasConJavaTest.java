package minijava.lexico;

import minijava.aplicacion.ModuloPrincipal;
import minijava.comun.salida.SalidaEnMemoria;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Puntos donde MiniJava se aparta de Java a propósito. Están todos en la tabla de
// agents/especificacion-lexica.md y van en el informe.
class DivergenciasConJavaTest {

    private AnalizadorLexico crear(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }

    private List<String> ejecutar(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipal(salida).ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // Java traduce los escapes unicode en todo el fuente antes de tokenizar (JLS
    // §3.3), comentarios incluidos; MiniJava sólo dentro de un literal. No es
    // teoría: un comentario de este proyecto dio "illegal unicode escape" en javac.
    @Test
    void unEscapeUnicodeMalFormadoEnUnComentarioNoEsError(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "// esto en Java no compila: \\u12G4\nv1\n");

        assertTrue(lineas.contains("[SinErrores]"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v1,2)"), lineas::toString);
    }

    // En Java el escape se vuelve un salto real y TERMINA el comentario; acá el
    // comentario sigue hasta el salto físico.
    @Test
    void unSaltoDeLineaEscapadoNoTerminaUnComentarioDeLinea(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "// \\u000A v1\nv2\n");

        assertTrue(lineas.contains("[SinErrores]"), lineas::toString);
        assertTrue(lineas.stream().noneMatch(l -> l.contains(",v1,")), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,2)"), lineas::toString);
    }

    // Por lo mismo, un escape de comilla no cierra el litString: son seis caracteres
    // del lexema. En Java esa línea no compila.
    @Test
    void unEscapeUnicodeDeComillaNoCierraElString(@TempDir Path dir) throws Exception {
        Token token = crear(dir, "\"a\\u0022b\" ").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"a\\u0022b\"", token.lexema());
    }

    // Character.isDigit los acepta, así que arrancan un intLiteral y cuentan para el
    // límite de nueve. Java no los admite.
    @Test
    void losDigitosArabeIndicosArrancanUnEntero(@TempDir Path dir) throws Exception {
        Token token = crear(dir, "\u0661\u0662\u0663 ").proximoToken();

        assertEquals(TipoDeToken.INT_LITERAL, token.tipo());
        assertEquals("\u0661\u0662\u0663", token.lexema());
    }

    // No puede EMPEZAR un identificador (isLowerCase/isUpperCase) pero sí
    // continuarlo (isLetterOrDigit). En Java sirve para las dos cosas.
    @Test
    void unaLetraSinCajaContinuaUnIdentificadorPeroNoLoEmpieza(@TempDir Path dir) throws Exception {
        Token token = crear(dir, "a\u4E2D ").proximoToken();
        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("a\u4E2D", token.lexema());

        List<String> lineas = ejecutar(dir, "\u4E2Dabc\n");
        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("[Error:")), lineas::toString);
        assertTrue(lineas.contains("(idMV,abc,1)"), lineas::toString);
    }

    // Fuera de un literal son dos char de Java, así que dan DOS errores; adentro
    // pasa entero.
    @Test
    void unParSurrogadoFueraDeUnLiteralDaDosErrores(@TempDir Path dir) throws IOException {
        String emoji = new String(Character.toChars(0x1F600));

        List<String> lineas = ejecutar(dir, "v1 " + emoji + " v2\n");

        assertEquals(2, lineas.stream().filter(l -> l.startsWith("[Error:")).count(),
                lineas::toString);
        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
    }

    @Test
    void unParSurrogadoDentroDeUnStringEsContenidoValido(@TempDir Path dir) throws Exception {
        String emoji = new String(Character.toChars(0x1F600));

        Token token = crear(dir, "\"hola " + emoji + "\" ").proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo());
        assertEquals("\"hola " + emoji + "\"", token.lexema());
    }

    // Blancos en Java y errores acá: esBlanco() sólo acepta espacio, tab y salto.
    @Test
    void elFormFeedNoEsUnBlanco(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "v1 " + (char) 12 + " v2\n");

        assertTrue(lineas.contains("[Error:\\f|1]"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
        assertTrue(lineas.contains("(idMV,v2,1)"), lineas::toString);
    }

    // javac salta el BOM inicial; acá es un carácter desconocido más.
    @Test
    void laMarcaDeOrdenDeBytesEsUnCaracterDesconocido(@TempDir Path dir) throws IOException {
        List<String> lineas = ejecutar(dir, "\uFEFFv1\n");

        assertTrue(lineas.stream().anyMatch(l -> l.startsWith("[Error:")), lineas::toString);
        assertTrue(lineas.contains("(idMV,v1,1)"), lineas::toString);
    }
}
