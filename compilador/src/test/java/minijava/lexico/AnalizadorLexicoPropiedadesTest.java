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
// sólo para los casos concretos de AnalizadorLexicoTest (agents/testing.md).
class AnalizadorLexicoPropiedadesTest {

    private static final int LIMITE_DE_SEGURIDAD = 100_000;

    // Uno solo reescrito en cada llamada: jqwik corre miles de intentos por
    // propiedad e inundaba /tmp con un archivo por intento.
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

    // Como ModuloPrincipal. Corta con AssertionError si supera el límite: eso sería
    // un ciclo infinito real.
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

    // K símbolos inválidos consecutivos dan exactamente K errores: ni más (por
    // consumir de más) ni un ciclo infinito (por no consumir nada).
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
