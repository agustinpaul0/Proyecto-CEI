package minijava.lexico;

import minijava.errores.ExcepcionLexica;
import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Invariantes de robustez que deben sostenerse para CUALQUIER entrada, no
// sólo para los casos concretos de AnalizadorLexicoTest (AGENTS.md §8).
class AnalizadorLexicoPropiedadesTest {

    private static final int LIMITE_DE_SEGURIDAD = 100_000;

    // Un único archivo temporal, reescrito en cada llamada, en vez de uno
    // por intento — jqwik corre miles de intentos por propiedad y crear un
    // archivo por intento inundaba /tmp con miles de archivos sin limpiar.
    private static final Path ARCHIVO_COMPARTIDO = crearArchivoCompartido();

    private static Path crearArchivoCompartido() {
        try {
            Path archivo = Files.createTempFile("minijava-propiedades-", ".mjava");
            archivo.toFile().deleteOnExit();
            return archivo;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private AnalizadorLexico crear(String fuente) throws IOException {
        Files.writeString(ARCHIVO_COMPARTIDO, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(ARCHIVO_COMPARTIDO.toString());
        return new AnalizadorLexico(manejador);
    }

    // Tokeniza con recuperación (igual que ModuloPrincipal) y devuelve los
    // tokens emitidos con éxito. Corta con AssertionError si supera el
    // límite de seguridad — eso indicaría un ciclo infinito real, un bug.
    private List<Token> tokenizarConRecuperacion(String fuente) throws IOException {
        AnalizadorLexico analizador = crear(fuente);
        List<Token> emitidos = new ArrayList<>();
        for (int i = 0; i < LIMITE_DE_SEGURIDAD; i++) {
            try {
                Token token = analizador.proximoToken();
                emitidos.add(token);
                if (token.tipo() == TipoDeToken.EOF) {
                    return emitidos;
                }
            } catch (ExcepcionLexica excepcion) {
            }
        }
        throw new AssertionError("No terminó tras " + LIMITE_DE_SEGURIDAD + " pasos: " + fuente);
    }

    @Property
    void siempreTerminaYNuncaLanzaOtraCosaQueExcepcionLexica(@ForAll("fuentesAleatorias") String fuente)
            throws IOException {
        assertDoesNotThrow(() -> tokenizarConRecuperacion(fuente));
    }

    @Property
    void lasLineasDeLosTokensEmitidosNuncaDecrecen(@ForAll("fuentesAleatorias") String fuente)
            throws IOException {
        List<Token> tokens = tokenizarConRecuperacion(fuente);
        int lineaAnterior = 1;
        for (Token token : tokens) {
            assertTrue(token.linea() >= lineaAnterior,
                    "línea decreció: " + token + " después de línea " + lineaAnterior);
            lineaAnterior = token.linea();
        }
    }

    @Property
    void siempreTerminaEnEofNoEnOtroTokenAunConErroresPorMedio(@ForAll("fuentesAleatorias") String fuente)
            throws IOException {
        List<Token> tokens = tokenizarConRecuperacion(fuente);
        assertFalse(tokens.isEmpty());
        assertEquals(TipoDeToken.EOF, tokens.get(tokens.size() - 1).tipo());
    }

    // Prueba directa del criterio de recuperación: cada error consume
    // exactamente el carácter que reporta, así que una corrida de K símbolos
    // inválidos consecutivos produce exactamente K errores — ni más (por
    // consumir de más) ni un ciclo infinito (por no consumir nada) — antes de
    // llegar a EOF. Ya no hay ningún paso de recuperación que invocar.
    @Property
    void unaCorridaDeKSimbolosInvalidosProduceExactamenteKErrores(
            @ForAll @IntRange(min = 1, max = 40) int cantidad) throws IOException {
        String fuente = "#".repeat(cantidad);
        AnalizadorLexico analizador = crear(fuente);
        int errores = 0;

        for (int i = 0; i < cantidad + 1; i++) {
            try {
                Token token = analizador.proximoToken();
                assertEquals(TipoDeToken.EOF, token.tipo());
                assertEquals(cantidad, errores);
                return;
            } catch (ExcepcionLexica excepcion) {
                errores++;
            }
        }
        fail("no llegó a EOF tras " + cantidad + " recuperaciones");
    }

    @Provide
    Arbitrary<String> fuentesAleatorias() {
        Arbitrary<Character> caracteres = Arbitraries.oneOf(
                Arbitraries.chars().range('a', 'z'),
                Arbitraries.chars().range('A', 'Z'),
                Arbitraries.chars().range('0', '9'),
                Arbitraries.of('{', '}', '(', ')', '"', '\'', '\\', '\n', ' ', '#',
                        '+', '-', '*', '/', '.', ';', '&', '|', '$', '_'));
        return caracteres.list().ofMaxSize(60).map(lista -> {
            StringBuilder sb = new StringBuilder();
            lista.forEach(sb::append);
            return sb.toString();
        });
    }

}
