package minijava.lexico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los cuatro tokens que NO figuran en «Aspectos Léxicos de MiniJava» y que
// existen sólo porque un logro de la Etapa 2 los necesita. Cada uno se prueba
// por los dos lados: con el logro prendido tiene que reconocerse, y con el logro
// apagado tiene que volver EXACTAMENTE al comportamiento de la Etapa 1.
class TokensDeLaEtapaDosTest {

    private static final ConfiguracionDelCompilador ETAPA_UNO =
            ConfiguracionDelCompilador.soloEtapa(1);

    private AnalizadorLexico analizador(Path dir, String fuente, ConfiguracionDelCompilador configuracion)
            throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador, configuracion);
    }

    private List<Token> tokenizar(Path dir, String fuente, ConfiguracionDelCompilador configuracion)
            throws IOException, ExcepcionLexica {
        AnalizadorLexico analizador = analizador(dir, fuente, configuracion);
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = analizador.proximoToken();
            tokens.add(token);
        } while (token.tipo() != TipoDeToken.EOF);
        return tokens;
    }

    // ---------------------------------------------------------------- for ---

    @Test
    void conElLogroDeForsLaPalabraForEsReservada(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "for", ConfiguracionDelCompilador.ninguno().con(Logro.FOR))
                .getFirst();

        assertEquals(TipoDeToken.PR_FOR, token.tipo());
        assertEquals("for", token.lexema());
    }

    @Test
    void sinElLogroDeForsLaPalabraForVuelveAserIdMV(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "for", ETAPA_UNO).getFirst();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("for", token.lexema());
    }

    // ------------------------------------------------------------ private ---

    @Test
    void conElLogroDeVisibilidadLaPalabraPrivateEsReservada(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "private", ConfiguracionDelCompilador.ninguno().con(Logro.VISIBILIDAD))
                .getFirst();

        assertEquals(TipoDeToken.PR_PRIVATE, token.tipo());
        assertEquals("private", token.lexema());
    }

    @Test
    void sinElLogroDeVisibilidadLaPalabraPrivateVuelveAserIdMV(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "private", ETAPA_UNO).getFirst();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
    }

    // Cada logro prende SÓLO su palabra: prender Fors! no puede colar `private`.
    @Test
    void cadaLogroPrendeUnicamenteSuPropiaPalabraReservada(@TempDir Path dir) throws Exception {
        var soloFor = ConfiguracionDelCompilador.ninguno().con(Logro.FOR);

        List<Token> tokens = tokenizar(dir, "for private", soloFor);

        assertEquals(TipoDeToken.PR_FOR, tokens.get(0).tipo());
        assertEquals(TipoDeToken.ID_MV, tokens.get(1).tipo());
    }

    // Prefijos y sufijos: `format` no es `for`, ni con el logro prendido.
    @ParameterizedTest
    @ValueSource(strings = {"format", "forma", "for1", "for_", "privateX", "myprivate", "For", "FOR"})
    void unIdentificadorQueSoloEmpiezaOTerminaComoLaPalabraSigueSiendoUnIdentificador(
            String fuente) throws Exception {
        Path dir = Files.createTempDirectory("mj");
        Token token = tokenizar(dir, fuente, ConfiguracionDelCompilador.todos()).getFirst();

        assertTrue(token.tipo() == TipoDeToken.ID_MV || token.tipo() == TipoDeToken.ID_CLASE,
                fuente + " -> " + token.tipo());
        assertEquals(fuente, token.lexema());
    }

    // ------------------------------------------------------------------ ? ---

    @Test
    void conElLogroDeTernarioElSignoDePreguntaEsUnToken(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "?", ConfiguracionDelCompilador.ninguno().con(Logro.TERNARIO))
                .getFirst();

        assertEquals(TipoDeToken.SIGNO_PREGUNTA, token.tipo());
        assertEquals("?", token.lexema());
    }

    @Test
    void sinElLogroDeTernarioElSignoDePreguntaVuelveAserUnErrorLexico(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = analizador(dir, "v1 ? v2", ETAPA_UNO);
        analizador.proximoToken();

        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(TipoDeErrorLexico.CARACTER_DESCONOCIDO, excepcion.getTipo());
        assertEquals("?", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
    }

    @Test
    void elTernarioCompletoSeTokenizaConSuSignoDePreguntaYSusDosPuntos(@TempDir Path dir)
            throws Exception {
        List<Token> tokens = tokenizar(dir, "a ? b : c", ConfiguracionDelCompilador.todos());

        assertEquals(List.of(TipoDeToken.ID_MV, TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.ID_MV,
                        TipoDeToken.DOS_PUNTOS, TipoDeToken.ID_MV, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // ----------------------------------------------------------------- -> ---

    @Test
    void conElLogroDeLambdasLaFlechaEsUnSoloToken(@TempDir Path dir) throws Exception {
        Token token = tokenizar(dir, "->", ConfiguracionDelCompilador.ninguno().con(Logro.LAMBDA))
                .getFirst();

        assertEquals(TipoDeToken.OP_FLECHA, token.tipo());
        assertEquals("->", token.lexema());
    }

    @Test
    void sinElLogroDeLambdasLaFlechaVuelveAserDosTokens(@TempDir Path dir) throws Exception {
        List<Token> tokens = tokenizar(dir, "a -> b", ETAPA_UNO);

        assertEquals(List.of(TipoDeToken.ID_MV, TipoDeToken.OP_RESTA, TipoDeToken.OP_MAYOR,
                        TipoDeToken.ID_MV, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // Maximal munch: con el logro prendido `-` sigue compitiendo con `--` y `->`,
    // y gana el más largo que exista. `-->` es `--` seguido de `>`, no `-` y `->`.
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "-    | op-",
            "--   | op_decremento",
            "->   | op_flecha",
            "-=   | op-",
            "---  | op_decremento",
    })
    void elMenosSigueEligiendoElLexemaMasLargoQueExiste(String fuente, String nombreEsperado)
            throws Exception {
        Path dir = Files.createTempDirectory("mj");
        Token token = tokenizar(dir, fuente, ConfiguracionDelCompilador.todos()).getFirst();

        assertEquals(nombreEsperado, token.tipo().getNombre());
    }

    @Test
    void laFlechaPegadaAOtrosTokensSeSeparaBien(@TempDir Path dir) throws Exception {
        List<Token> tokens = tokenizar(dir, "(x)->x+1", ConfiguracionDelCompilador.todos());

        assertEquals(List.of(TipoDeToken.PAREN_A, TipoDeToken.ID_MV, TipoDeToken.PAREN_C,
                        TipoDeToken.OP_FLECHA, TipoDeToken.ID_MV, TipoDeToken.OP_SUMA,
                        TipoDeToken.INT_LITERAL, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // El '>' de la flecha no puede robarle el sufijo a `>=`: son estados distintos.
    @Test
    void elSufijoDeLaFlechaNoAfectaAlOperadorMayorIgual(@TempDir Path dir) throws Exception {
        List<Token> tokens = tokenizar(dir, "a >= b", ConfiguracionDelCompilador.todos());

        assertEquals(TipoDeToken.OP_MAYOR_IGUAL, tokens.get(1).tipo());
    }

    // ------------------------------------------------------- combinaciones ---

    // Los cuatro logros conviven sin pisarse: es la exigencia de que cada uno sea
    // un interruptor independiente.
    @Test
    void losCuatroLogrosPrendidosALaVezReconocenLosCuatroTokens(@TempDir Path dir) throws Exception {
        var configuracion = ConfiguracionDelCompilador.ninguno()
                .con(Logro.FOR, Logro.VISIBILIDAD, Logro.TERNARIO, Logro.LAMBDA);

        List<Token> tokens = tokenizar(dir, "private for ? ->", configuracion);

        assertEquals(List.of(TipoDeToken.PR_PRIVATE, TipoDeToken.PR_FOR,
                        TipoDeToken.SIGNO_PREGUNTA, TipoDeToken.OP_FLECHA, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    @Test
    void conCeroLogrosNingunoDeLosCuatroSeReconoce(@TempDir Path dir) throws Exception {
        var ninguno = ConfiguracionDelCompilador.ninguno();

        List<Token> tokens = tokenizar(dir, "private for ->", ninguno);

        assertEquals(List.of(TipoDeToken.ID_MV, TipoDeToken.ID_MV, TipoDeToken.OP_RESTA,
                        TipoDeToken.OP_MAYOR, TipoDeToken.EOF),
                tokens.stream().map(Token::tipo).toList());
    }

    // ----------------------------------------------------------- la tabla ---

    @Test
    void laTablaDeclaraElLogroDeCadaUnoDeLosCuatroTokensNuevos() {
        assertEquals(Logro.FOR, TablaDeLexemas.logroQueLoHabilita(TipoDeToken.PR_FOR).orElseThrow());
        assertEquals(Logro.VISIBILIDAD,
                TablaDeLexemas.logroQueLoHabilita(TipoDeToken.PR_PRIVATE).orElseThrow());
        assertEquals(Logro.TERNARIO,
                TablaDeLexemas.logroQueLoHabilita(TipoDeToken.SIGNO_PREGUNTA).orElseThrow());
        assertEquals(Logro.LAMBDA,
                TablaDeLexemas.logroQueLoHabilita(TipoDeToken.OP_FLECHA).orElseThrow());
    }

    // Los 56 de la Etapa 1 no dependen de ningún logro: se reconocen siempre.
    @Test
    void ningunTokenDeLaEtapaUnoQuedaCondicionadoAUnLogro() {
        List<TipoDeToken> condicionados = new ArrayList<>();
        for (TipoDeToken tipo : TipoDeToken.values()) {
            if (TablaDeLexemas.logroQueLoHabilita(tipo).isPresent()) {
                condicionados.add(tipo);
            }
        }

        assertEquals(List.of(TipoDeToken.PR_FOR, TipoDeToken.PR_PRIVATE,
                TipoDeToken.OP_FLECHA, TipoDeToken.SIGNO_PREGUNTA), condicionados);
    }

    @Test
    void unTokenSinLogroEstaHabilitadoInclusoSinNingunLogroPrendido() {
        assertTrue(TablaDeLexemas.estaHabilitado(TipoDeToken.PR_CLASS,
                ConfiguracionDelCompilador.ninguno()));
        assertFalse(TablaDeLexemas.estaHabilitado(TipoDeToken.PR_FOR,
                ConfiguracionDelCompilador.ninguno()));
    }
}
