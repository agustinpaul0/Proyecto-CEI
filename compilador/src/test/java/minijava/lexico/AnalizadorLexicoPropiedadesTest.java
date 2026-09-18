package minijava.lexico;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.tokens.Token;
import minijava.lexico.tokens.TipoDeToken;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import net.jqwik.api.Tag;

// Invariantes de robustez que deben sostenerse para CUALQUIER entrada, no
// sólo para los casos concretos de las suites AnalizadorLexico*Test
// (documentacion/compartido/estrategia-de-testing.md).
@Tag("lexico")
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

    // Como ModuloPrincipalLexico. Corta con AssertionError si supera el límite: eso sería
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

    // --- La regla de cierre del litString (Aspectos Léxicos §2.3.3) ---

    // Cualquier contenido, con cualquier carácter tras una barra, es un litString
    // válido mientras la comilla de cierre no quede antecedida por una barra y no
    // haya un escape Unicode que validar. No hay conjunto de escapes permitidos:
    // si esta propiedad empieza a fallar, alguien volvió a introducir uno.
    @Property(tries = 500)
    void unStringSinBarraFinalNiUnicodeEsSiempreUnUnicoLitString(
            @ForAll("contenidosDeString") String contenido) throws Exception {
        String fuente = "\"" + contenido + "\"";
        AnalizadorLexico analizador = crear(fuente);

        Token token = analizador.proximoToken();

        assertEquals(TipoDeToken.LIT_STRING, token.tipo(), fuente);
        assertEquals(fuente, token.lexema());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // Y el reverso: agregarle una barra justo antes de la comilla de cierre lo
    // rompe siempre, con el lexema crudo y sin decodificar.
    @Property(tries = 500)
    void unaBarraAntesDeLaComillaDeCierreSiempreDejaElStringSinCerrar(
            @ForAll("contenidosDeString") String contenido) throws Exception {
        String fuente = "\"" + contenido + "\\\"";
        AnalizadorLexico analizador = crear(fuente);

        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(minijava.lexico.errores.TipoDeErrorLexico.STRING_SIN_CERRAR, excepcion.getTipo());
        assertEquals(fuente, excepcion.getLexemaOfensivo());
    }

    // Contenido arbitrario sin saltos de línea ni comillas dobles sueltas, con
    // barras seguidas de cualquier cosa menos 'u' (que sí se valida) y sin quedar
    // una barra pegada al final.
    @Provide
    Arbitrary<String> contenidosDeString() {
        Arbitrary<String> comunes = Arbitraries.strings()
                .withChars('a', 'z').withChars('0', '9').withChars(' ', '!', '#', '%', 'ñ')
                .ofMaxLength(6);
        Arbitrary<String> escapados = Arbitraries.chars()
                .range('a', 't').range('v', 'z').range('0', '9')
                .with('\\', '"', '\'', 'n', '!')
                .map(c -> "\\" + c);
        return Combinators.combine(comunes, escapados, comunes)
                .as((izquierda, escape, derecha) -> izquierda + escape + derecha)
                // Una barra pegada al cierre lo dejaría sin cerrar: es la otra propiedad.
                .filter(texto -> !texto.endsWith("\\"));
    }
}
