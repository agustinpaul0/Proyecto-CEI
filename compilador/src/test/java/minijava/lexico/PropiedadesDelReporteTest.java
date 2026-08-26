package minijava.lexico;

import minijava.aplicacion.ModuloPrincipal;
import minijava.errores.ExcepcionLexica;
import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;
import minijava.salida.SalidaEnMemoria;
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

// Invariantes del REPORTE y de la recuperación que deben sostenerse para
// cualquier entrada. Complementan AnalizadorLexicoPropiedadesTest, que mira
// el analizador; acá se mira la salida completa del Módulo Principal, que es
// lo que efectivamente ve la cátedra.
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
        new ModuloPrincipal(salida).ejecutar(ARCHIVO.toString());
        return salida.getLineas();
    }

    // El contrato de la cátedra es un elemento por línea. Un salto de línea
    // embebido en un lexema partiría [Error:Lexema|NroLinea] en dos y
    // rompería el formato; por eso los caracteres de control se escapan.
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

    // El '^' del Reporte de Error Elegante tiene que caer SOBRE un carácter de
    // la línea que se acaba de imprimir, nunca en el aire después del final.
    // Fue un bug real (ver documentacion/manejo-de-errores.md §5) y esta
    // propiedad lo cierra para cualquier entrada, no sólo para los casos
    // conocidos.
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

    // O hubo al menos un error, o el análisis fue exitoso: nunca las dos
    // cosas, y nunca ninguna.
    @Property
    void oHayErroresOHaySinErroresPeroNuncaAmbos(@ForAll("fuentes") String fuente) throws IOException {
        List<String> lineas = ejecutar(fuente);

        boolean huboErrores = lineas.stream().anyMatch(l -> l.startsWith("[Error:"));
        boolean exito = lineas.contains("[SinErrores]");

        assertTrue(huboErrores || exito, lineas::toString);
        assertFalse(huboErrores && exito, lineas::toString);
    }

    // Round-trip: si el fuente no tiene blancos ni comentarios, absolutamente
    // TODO carácter tiene que aparecer en algún lexema — de token emitido o de
    // error fatal. Es la demostración de que la recuperación ya no se traga
    // nada: antes del criterio nuevo esta propiedad fallaba con cualquier '&'
    // o entero largo pegado a otro token.
    //
    // Se corre contra el analizador y no contra la salida impresa porque los
    // errores NO fatales (escape inválido de string) reportan un lexema que es
    // PREFIJO del literal que después se emite completo: contarlos sería
    // contar dos veces el mismo texto.
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
                analizador.tomarErroresNoFatales();
                if (token.tipo() == TipoDeToken.EOF) {
                    assertEquals(fuente, reconstruido.toString());
                    return;
                }
                reconstruido.append(token.lexema());
            } catch (ExcepcionLexica excepcion) {
                analizador.tomarErroresNoFatales();
                reconstruido.append(excepcion.getLexemaOfensivo());
            }
        }
        throw new AssertionError("no llego a EOF: " + fuente);
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

    // Sin blancos ni barras: los blancos y los comentarios se descartan y no
    // sobreviven en ningún lexema, así que romperían el round-trip por
    // diseño, no por un defecto.
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
