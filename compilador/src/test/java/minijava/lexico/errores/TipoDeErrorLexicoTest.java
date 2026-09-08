package minijava.lexico.errores;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.tokens.TipoDeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Un caso por cada TipoDeErrorLexico, validado por tipo y localización y nunca por
// el texto del mensaje, como pide agents/testing.md.
class TipoDeErrorLexicoTest {

    // fuente -> (tipo, línea, columna del carácter señalado por el ^). Criterio de
    // javac, salvo charLiteral vacío: su comilla de cierre es el carácter que lo
    // vuelve vacío.
    private record Caso(String fuente, int linea, int columna) {
    }

    private static final Map<TipoDeErrorLexico, Caso> CASOS = new EnumMap<>(Map.ofEntries(
            Map.entry(TipoDeErrorLexico.CARACTER_DESCONOCIDO, new Caso("v1 # v2", 1, 4)),
            Map.entry(TipoDeErrorLexico.CHAR_VACIO, new Caso("''", 1, 2)),
            Map.entry(TipoDeErrorLexico.CHAR_MAL_FORMADO, new Caso("'ab'", 1, 1)),
            Map.entry(TipoDeErrorLexico.CHAR_SIN_CERRAR, new Caso("'a\n", 1, 1)),
            Map.entry(TipoDeErrorLexico.STRING_SIN_CERRAR, new Caso("\"hola\n", 1, 1)),
            Map.entry(TipoDeErrorLexico.UNICODE_MAL_FORMADO, new Caso("'\\u12G4'", 1, 6)),
            Map.entry(TipoDeErrorLexico.COMENTARIO_SIN_CERRAR, new Caso("/* sin cerrar", 1, 1)),
            Map.entry(TipoDeErrorLexico.ENTERO_DEMASIADO_LARGO, new Caso("1234567890 ", 1, 1)),
            Map.entry(TipoDeErrorLexico.OPERADOR_INCOMPLETO, new Caso("a & b", 1, 3)),
            Map.entry(TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS, new Caso("1e ", 1, 1))));

    private AnalizadorLexico crear(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }

    @ParameterizedTest
    @EnumSource(TipoDeErrorLexico.class)
    void cadaTipoDeErrorTieneUnCasoQueLoProduceEnLaPosicionCorrecta(TipoDeErrorLexico tipo,
                                                                    @TempDir Path dir) throws Exception {
        Caso caso = CASOS.get(tipo);
        assertNotNull(caso, () -> "falta un caso para " + tipo);

        AnalizadorLexico analizador = crear(dir, caso.fuente());

        // El error no tiene por qué estar en el primer token.
        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class, () -> {
            for (int i = 0; i < 100; i++) {
                analizador.proximoToken();
            }
        });

        assertEquals(tipo, excepcion.getTipo(), () -> "fuente: " + caso.fuente());
        assertEquals(caso.linea(), excepcion.getLinea(), () -> "fuente: " + caso.fuente());
        assertEquals(caso.columna(), excepcion.getColumna(), () -> "fuente: " + caso.fuente());
    }

    // Fin de línea o de archivo dan "sin cerrar"; otro carácter da "mal formado".
    // Los dos salen de EstadoCharCerrando, que decide mirando el lookahead.
    @Test
    void charCerradoPorUnCaracterEsMalFormadoYCortadoPorFinDeLineaEsSinCerrar(@TempDir Path dir)
            throws IOException {
        var malFormado = assertThrows(ExcepcionLexica.class,
                () -> crear(dir, "'ab'").proximoToken());

        var sinCerrar = assertThrows(ExcepcionLexica.class,
                () -> crear(dir, "'a\n").proximoToken());
        var sinCerrarPorEof = assertThrows(ExcepcionLexica.class,
                () -> crear(dir, "'a").proximoToken());

        assertEquals(TipoDeErrorLexico.CHAR_MAL_FORMADO, malFormado.getTipo());
        assertEquals(TipoDeErrorLexico.CHAR_SIN_CERRAR, sinCerrar.getTipo());
        assertEquals(TipoDeErrorLexico.CHAR_SIN_CERRAR, sinCerrarPorEof.getTipo());
    }

    // Aborta el literal: se descarta hasta la comilla de cierre, sin emitir
    // token. Es el único error de escape que queda dentro de un litString.
    @Test
    void unicodeMalFormadoEnStringDescartaElLiteralYNoEmiteToken(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crear(dir, "\"hola \\" + "u12G4 mundo\"");

        var excepcion = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(TipoDeErrorLexico.UNICODE_MAL_FORMADO, excepcion.getTipo());
        assertEquals("\"hola \\" + "u12", excepcion.getLexemaOfensivo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // Una barra seguida de cualquier otra cosa no es un error: §2.3.3 no define
    // un conjunto de escapes válidos, sólo dice qué comilla cierra.
    @Test
    void unaBarraSeguidaDeCualquierCosaNoEsErrorEnUnString(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crear(dir, "\"hola \\q mundo\"");

        assertEquals("\"hola \\q mundo\"", analizador.proximoToken().lexema());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }


    // El de CARACTER_DESCONOCIDO es el de la slide 43 y no puede cambiar; el resto
    // tiene que nombrar su construcción.
    @Test
    void soloElCaracterDesconocidoConservaLaFraseDeLaCatedra() {
        assertEquals("# no es un símbolo válido",
                TipoDeErrorLexico.CARACTER_DESCONOCIDO.describir("#"));

        for (TipoDeErrorLexico tipo : TipoDeErrorLexico.values()) {
            if (tipo == TipoDeErrorLexico.CARACTER_DESCONOCIDO) {
                continue;
            }
            String descripcion = tipo.describir("x");
            assertTrue(!descripcion.contains("no es un símbolo válido"),
                    () -> tipo + " repite la frase genérica: " + descripcion);
            assertTrue(descripcion.contains("x"),
                    () -> tipo + " no incluye el lexema: " + descripcion);
        }
    }
}
