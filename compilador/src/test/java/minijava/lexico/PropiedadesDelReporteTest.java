package minijava.lexico;

import minijava.aplicacion.ModuloPrincipalLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.comun.salida.SalidaEnMemoria;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Invariantes del reporte y de la recuperación para cualquier entrada. A
// diferencia de AnalizadorLexicoPropiedadesTest, acá se mira la salida completa
// del Módulo Principal, que es lo que ve la cátedra.
class PropiedadesDelReporteTest {

    private static final String PREFIJO_DETALLE = "Detalle: ";
    private static final Path ARCHIVO = crearArchivo();

    private static Path crearArchivo() {
        try {
            Path archivo = Files.createTempFile("minijava-reporte-", ".mjava");
            archivo.toFile().deleteOnExit();
            return archivo;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private List<String> ejecutar(String fuente) throws IOException {
        Files.writeString(ARCHIVO, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalLexico(salida).ejecutar(ARCHIVO.toString());
        return salida.getLineas();
    }

    // Un salto embebido en un lexema partiría [Error:Lexema|NroLinea] en dos.
    @Property
    void ningunaLineaDeSalidaContieneUnSaltoDeLinea(@ForAll("fuentes") String fuente) throws IOException {
        List<String> lineas = ejecutar(fuente);

        assertTrue(lineas.stream().noneMatch(l -> l.indexOf('\n') >= 0 || l.indexOf('\r') >= 0),
                lineas::toString);
    }

    @Property
    void ningunCodigoDeErrorLlevaUnCaracterDeControl(@ForAll("fuentes") String fuente) throws IOException {
        List<String> lineas = ejecutar(fuente);

        assertTrue(lineas.stream()
                        .filter(l -> l.startsWith("[Error:"))
                        .noneMatch(l -> l.chars().anyMatch(c -> c < 0x20)),
                lineas::toString);
    }

    // El '^' tiene que caer SOBRE un carácter de la línea recién impresa, nunca en
    // el aire después del final. Fue un bug real (manejo-de-errores.md §5).
    @Property
    void elCaretNuncaCaeMasAllaDelFinalDeLaLineaDeDetalle(@ForAll("fuentes") String fuente)
            throws IOException {
        List<String> lineas = ejecutar(fuente);

        for (int i = 0; i < lineas.size() - 1; i++) {
            String detalle = lineas.get(i);
            String caret = lineas.get(i + 1);
            if (!detalle.startsWith(PREFIJO_DETALLE) || !caret.trim().equals("^")) {
                continue;
            }
            int columnaDelCaret = caret.indexOf('^') - PREFIJO_DETALLE.length();
            int largoDeLaLinea = detalle.length() - PREFIJO_DETALLE.length();
            int indice = i;
            assertTrue(columnaDelCaret >= 0 && columnaDelCaret < largoDeLaLinea,
                    () -> "el caret cae fuera de la linea impresa (columna " + columnaDelCaret
                            + ", largo " + largoDeLaLinea + ")\n"
                            + lineas.get(indice) + "\n" + lineas.get(indice + 1));
        }
    }

    // O hubo error, o hubo éxito: nunca las dos, nunca ninguna.
    @Property
    void oHayErroresOHaySinErroresPeroNuncaAmbos(@ForAll("fuentes") String fuente) throws IOException {
        List<String> lineas = ejecutar(fuente);

        boolean huboErrores = lineas.stream().anyMatch(l -> l.startsWith("[Error:"));
        boolean exito = lineas.contains("[SinErrores]");

        assertTrue(huboErrores || exito, lineas::toString);
        assertFalse(huboErrores && exito, lineas::toString);
    }

    // Round-trip: sin blancos ni comentarios, todo carácter tiene que aparecer en
    // algún lexema —o en el resto del litString que se descarta a propósito—. Es la
    // demostración de que la recuperación no se traga nada por accidente.
    @Property
    void todoCaracterDelFuenteApareceEnAlgunLexema(@ForAll("fuentesSinBlancos") String fuente)
            throws IOException {
        Files.writeString(ARCHIVO, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(ARCHIVO.toString());
        AnalizadorLexico analizador = new AnalizadorLexico(manejador);
        StringBuilder reconstruido = new StringBuilder();

        for (int i = 0; i < 10_000; i++) {
            try {
                Token token = analizador.proximoToken();
                if (token.tipo() == TipoDeToken.EOF) {
                    assertEquals(fuente, reconstruido.toString());
                    return;
                }
                reconstruido.append(token.lexema());
            } catch (ExcepcionLexica excepcion) {
                reconstruido.append(excepcion.getLexemaOfensivo());
                if (resincronizoElLitString(excepcion)) {
                    reconstruido.append(descarteDeLitString(fuente, reconstruido.length()));
                }
            }
        }
        throw new AssertionError("no llego a EOF: " + fuente);
    }

    // El único que resincroniza, y sólo dentro de un litString: en un
    // charLiteral el lexema empieza con comilla simple y la recuperación es
    // estructural.
    private static boolean resincronizoElLitString(ExcepcionLexica excepcion) {
        return excepcion.getTipo() == TipoDeErrorLexico.UNICODE_MAL_FORMADO
                && excepcion.getLexemaOfensivo().startsWith("\"");
    }

    // La regla de RecuperacionDeLitString reescrita a propósito: si el léxico deja
    // de descartar lo que dice, esta propiedad lo detecta en vez de acompañarlo.
    private static String descarteDeLitString(String fuente, int desde) {
        int hasta = desde;
        while (hasta < fuente.length() && fuente.charAt(hasta) != '\n') {
            char actual = fuente.charAt(hasta);
            hasta++;
            if (actual == '"') {
                break;
            }
            if (actual == '\\' && hasta < fuente.length() && fuente.charAt(hasta) != '\n') {
                hasta++;
            }
        }
        return fuente.substring(desde, hasta);
    }

    @Provide
    Arbitrary<String> fuentes() {
        Arbitrary<Character> caracteres = Arbitraries.of(
                'a', 'z', 'A', 'Z', '0', '9', '_',
                ' ', '\t', '\n',
                '\'', '"', '\\', '/', '*',
                '{', '}', '(', ')', '[', ']', ';', ',', '.', ':',
                '+', '-', '<', '>', '=', '!', '&', '|', '%',
                '#', '@', '?', '~', '^', '$', 'e', 'u', 'f');
        return caracteres.list().ofMaxSize(60).map(cs -> {
            StringBuilder sb = new StringBuilder();
            cs.forEach(sb::append);
            return sb.toString();
        });
    }

    // Sin blancos ni barras: no sobreviven en ningún lexema y romperían el
    // round-trip por diseño, no por un defecto.
    @Provide
    Arbitrary<String> fuentesSinBlancos() {
        Arbitrary<Character> caracteres = Arbitraries.of(
                'a', 'z', 'A', 'Z', '0', '9', '_',
                '\'', '"', '\\',
                '{', '}', '(', ')', '[', ']', ';', ',', '.', ':',
                '+', '-', '<', '>', '=', '!', '&', '|', '%',
                '#', '@', '?', '~', '^', '$', 'e', 'u', 'f');
        return caracteres.list().ofMaxSize(40).map(cs -> {
            StringBuilder sb = new StringBuilder();
            cs.forEach(sb::append);
            return sb.toString();
        });
    }
}
