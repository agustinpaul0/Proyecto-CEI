package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoBlancosYEofTest extends BaseDeAnalisisLexico {

    // Blancos, saltos de línea y el token EOF: dónde cae su línea.

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

}
