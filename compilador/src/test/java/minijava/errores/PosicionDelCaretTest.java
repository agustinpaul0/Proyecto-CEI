package minijava.errores;

import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Dónde apunta el '^' del Reporte de Error Elegante, caso por caso.
//
// El criterio no es nuestro: es el de javac, y la regla que sigue es una sola.
// Si el mensaje habla de un CARÁCTER ofensivo puntual, el '^' va a ese
// carácter; si habla de la CONSTRUCCIÓN ENTERA que falló, el '^' va a donde
// esa construcción empieza. La regla vale porque "todo lo que NO esté
// especificado para MiniJava tiene comportamiento por defecto igual al de
// Java" (etapas/etapa-1/README.md) y la cátedra no dice nada de columnas — de
// hecho ni siquiera pide la columna, es un agregado del proyecto.
//
// Cada caso de abajo lleva el diagnóstico de javac que lo respalda, obtenido
// EJECUTÁNDOLO (protocolo de agents/investigacion.md), no de memoria. Este es
// el test que impide que el criterio vuelva a divergir por partes, que es
// exactamente lo que había pasado: la mitad de los errores señalaba el inicio
// de la construcción y la otra mitad el último carácter consumido.
class PosicionDelCaretTest {

    private record Caso(String descripcion, String fuente, int columna, String segunJavac) {
        @Override
        public String toString() {
            return descripcion;
        }
    }

    static Stream<Caso> casos() {
        return Stream.of(
                // --- El mensaje habla de un carácter puntual: el '^' va ahí ---
                new Caso("carácter desconocido", "v1 # v2", 4,
                        "illegal character: '#' — señala el '#'"),
                new Caso("escape inválido de litString", "\"a\\q b\\z c\"", 4,
                        "illegal escape character — señala la 'q', no la barra"),
                new Caso("escape unicode con hexadecimal inválido", "'\\u12G4'", 6,
                        "illegal unicode escape — señala la 'G'"),

                // --- El mensaje habla de la construcción entera: el '^' va a su inicio ---
                new Caso("charLiteral vacío", "''", 1,
                        "empty character literal — señala la comilla de apertura"),
                new Caso("charLiteral sin cerrar", "'a", 1,
                        "unclosed character literal — señala la comilla de apertura"),
                new Caso("charLiteral con más de un carácter", "'ab'", 1,
                        "unclosed character literal — señala la comilla de apertura"),
                new Caso("litString sin cerrar", "\"hola sin cerrar", 1,
                        "unclosed string literal — señala la comilla de apertura"),
                new Caso("intLiteral de más de 9 dígitos", "1234567890", 1,
                        "integer number too large — señala el primer dígito"),
                new Caso("exponente sin dígitos", "1e", 1,
                        "malformed floating-point literal — señala el primer carácter"),
                new Caso("comentario multilínea sin cerrar", "/* sin cerrar", 1,
                        "unclosed comment — señala la barra de apertura"));
    }

    @ParameterizedTest
    @MethodSource("casos")
    void elCaretApuntaDondeApuntaJavac(Caso caso, @TempDir Path dir) throws IOException {
        ExcepcionLexica error = primerError(dir, caso.fuente());

        assertNotNull(error, () -> "el fuente no produjo ningún error: " + caso.fuente());
        assertEquals(caso.columna(), error.getColumna(),
                () -> caso.fuente() + " — javac: " + caso.segunJavac());
        assertEquals(1, error.getLinea(), caso::fuente);
    }

    // La invariante que hace útil a la columna: el '^' siempre cae sobre un
    // carácter que la línea impresa debajo del "Detalle:" realmente contiene.
    // Una columna más allá del final deja el caret señalando el aire.
    @ParameterizedTest
    @MethodSource("casos")
    void elCaretNuncaCaeFueraDeLaLineaImpresa(Caso caso, @TempDir Path dir) throws IOException {
        ExcepcionLexica error = primerError(dir, caso.fuente());

        assertNotNull(error);
        int indice = error.getColumna() - 1;
        assertTrue(indice >= 0 && indice < error.getLineaFuente().length(),
                () -> "columna " + error.getColumna() + " sobre la línea «"
                        + error.getLineaFuente() + "»");
    }

    // Cada litString roto reporta la columna de SU carácter escapado, sin
    // arrastrar la del anterior. Son dos literales y no uno solo con dos
    // escapes inválidos, porque al primer escape inválido el literal se
    // descarta entero: dentro de un mismo literal nunca hay un segundo error.
    @org.junit.jupiter.api.Test
    void cadaLitStringRotoDeUnaMismaLineaReportaSuPropiaColumna(@TempDir Path dir)
            throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, "\"a\\q\" \"b\\z\"");
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        AnalizadorLexico analizador = new AnalizadorLexico(manejador);

        ExcepcionLexica primero = assertThrows(ExcepcionLexica.class, analizador::proximoToken);
        ExcepcionLexica segundo = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(4, primero.getColumna());
        assertEquals(10, segundo.getColumna());
    }

    private ExcepcionLexica primerError(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        AnalizadorLexico analizador = new AnalizadorLexico(manejador);
        try {
            for (int i = 0; i < 100; i++) {
                analizador.proximoToken();
            }
            return null;
        } catch (ExcepcionLexica fatal) {
            return fatal;
        }
    }
}
