package minijava.errores;

import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.tokens.TipoDeToken;
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

// Un caso por cada TipoDeErrorLexico, validado por TIPO y LOCALIZACIÓN, que
// es lo que pide agents/testing.md ("los errores esperados se validan por tipo
// y localización, nunca por el texto completo del mensaje"). Antes de que
// existiera el enum esto era imposible: los 14 puntos de falla compartían una
// única frase.
class TipoDeErrorLexicoTest {

    // fuente -> (tipo esperado, línea, columna del carácter señalado por el ^)
    //
    // El criterio de la columna es el de javac, verificado ejecutándolo: si el
    // mensaje habla de un carácter ofensivo puntual (un símbolo desconocido,
    // un escape inválido, un dígito hexadecimal que no lo es), el ^ va a ese
    // carácter; si habla de la construcción entera que falló (un literal sin
    // cerrar o vacío, un intLiteral de más de 9 dígitos, un exponente sin
    // dígitos), el ^ va a donde la construcción empieza.
    private record Caso(String fuente, int linea, int columna) {
    }

    private static final Map<TipoDeErrorLexico, Caso> CASOS = new EnumMap<>(Map.ofEntries(
            Map.entry(TipoDeErrorLexico.CARACTER_DESCONOCIDO, new Caso("v1 # v2", 1, 4)),
            Map.entry(TipoDeErrorLexico.CHAR_VACIO, new Caso("''", 1, 1)),
            Map.entry(TipoDeErrorLexico.CHAR_MAL_FORMADO, new Caso("'ab'", 1, 1)),
            Map.entry(TipoDeErrorLexico.CHAR_SIN_CERRAR, new Caso("'a\n", 1, 1)),
            Map.entry(TipoDeErrorLexico.STRING_SIN_CERRAR, new Caso("\"hola\n", 1, 1)),
            Map.entry(TipoDeErrorLexico.UNICODE_MAL_FORMADO, new Caso("'\\u12G4'", 1, 6)),
            Map.entry(TipoDeErrorLexico.COMENTARIO_SIN_CERRAR, new Caso("/* sin cerrar", 1, 1)),
            Map.entry(TipoDeErrorLexico.ENTERO_DEMASIADO_LARGO, new Caso("1234567890 ", 1, 1)),
            Map.entry(TipoDeErrorLexico.OPERADOR_INCOMPLETO, new Caso("a & b", 1, 3)),
            Map.entry(TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS, new Caso("1e ", 1, 1)),
            Map.entry(TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, new Caso("\"a\\q\"", 1, 4))));

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

        // El error no tiene por que estar en el primer token: se piden tokens
        // hasta que aparezca.
        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class, () -> {
            for (int i = 0; i < 100; i++) {
                analizador.proximoToken();
            }
        });

        assertEquals(tipo, excepcion.getTipo(), () -> "fuente: " + caso.fuente());
        assertEquals(caso.linea(), excepcion.getLinea(), () -> "fuente: " + caso.fuente());
        assertEquals(caso.columna(), excepcion.getColumna(), () -> "fuente: " + caso.fuente());
    }

    // La distinción que pidió el usuario: si el literal se interrumpió por fin
    // de línea o fin de archivo el reporte dice "sin cerrar"; si lo
    // interrumpió un carácter cualquiera dice "mal formado". Los dos salen del
    // mismo estado (EstadoCharCerrando), que decide mirando el lookahead.
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

    // Un escape inválido aborta el literal: se reporta el primero y se
    // descarta lo que queda hasta la comilla de cierre, sin emitir token. Es
    // resincronización en modo pánico, con la comilla de cierre como token de
    // sincronización — criterio indicado por el profesor de la materia.
    @Test
    void escapeInvalidoDeStringDescartaElLiteralYNoEmiteToken(@TempDir Path dir) throws Exception {
        AnalizadorLexico analizador = crear(dir, "\"hola \\q mundo\"");

        var excepcion = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, excepcion.getTipo());
        assertEquals("\"hola \\q", excepcion.getLexemaOfensivo());
        assertEquals(TipoDeToken.EOF, analizador.proximoToken().tipo());
    }

    // El fragmento ofensivo tiene que viajar desde el estado que detecta el
    // error, no armarse en el formateador: es la única parte del reporte que
    // sabe qué carácter venía después de la barra invertida.
    @Test
    void elEscapeInvalidoViajaConLaSecuenciaOfensivaAdemasDelLexemaArmado(@TempDir Path dir)
            throws Exception {
        AnalizadorLexico analizador = crear(dir, "\"hola \\q mundo\"");

        var error = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        // Lo que exige Pautas: el lexema es el armado hasta el error.
        assertEquals("\"hola \\q", error.getLexemaOfensivo());
        // Lo que agrega el reporte elegante: cuál de sus caracteres está mal.
        assertEquals("\\q", error.getFragmentoOfensivo());
    }

    // Sin fragmento, describir() de dos argumentos tiene que dar exactamente
    // lo mismo que el de uno: es el caso de los otros diez tipos.
    @Test
    void describirSinFragmentoEsIgualAlMensajeDeUnSoloDato() {
        for (TipoDeErrorLexico tipo : TipoDeErrorLexico.values()) {
            assertEquals(tipo.describir("x"), tipo.describir("x", null), tipo::name);
        }
    }

    // El mensaje de CARACTER_DESCONOCIDO es el de la slide 43 y no puede
    // cambiar; el resto tiene que decir de qué construcción habla, no repetir
    // la frase genérica.
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
