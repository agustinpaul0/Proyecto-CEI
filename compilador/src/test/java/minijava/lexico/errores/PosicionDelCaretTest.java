package minijava.lexico.errores;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
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

// Dónde apunta el '^', caso por caso. El criterio es el de javac: un carácter
// ofensivo puntual lleva el '^' encima; una construcción entera, a su primer
// carácter, salvo charLiteral vacío, que señala su comilla de cierre. Cada caso
// cita el diagnóstico de javac que lo respalda, obtenido ejecutándolo.
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
                new Caso("escape unicode con hexadecimal inválido", "'\\u12G4'", 6,
                        "illegal unicode escape — señala la 'G'"),

                // --- El carácter de cierre es el que hace vacío al charLiteral ---
                new Caso("charLiteral vacío", "''", 2,
                        "empty character literal — señala la comilla de cierre"),
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
                        "unclosed comment — señala la barra de apertura"),
                // El caso que distingue "la apertura" de "la columna 1". Corrida real:
                //   T.java:1: error: unclosed comment
                //   class T { } /*
                //               ^
                new Caso("comentario abierto en la última columna", "v2 /*", 4,
                        "unclosed comment — señala la barra de apertura, no la columna 1"));
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

    // El '^' siempre cae sobre un carácter que la línea del "Detalle:" contiene;
    // más allá del final queda señalando el aire.
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

    // Cada litString roto reporta la columna de SU escape Unicode fallado. Son
    // dos literales y no uno con dos errores, porque el primero se descarta.
    @org.junit.jupiter.api.Test
    void cadaLitStringRotoDeUnaMismaLineaReportaSuPropiaColumna(@TempDir Path dir)
            throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, "\"a\\" + "u12G4\" \"b\\" + "u34Z6\"");
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        AnalizadorLexico analizador = new AnalizadorLexico(manejador);

        ExcepcionLexica primero = assertThrows(ExcepcionLexica.class, analizador::proximoToken);
        ExcepcionLexica segundo = assertThrows(ExcepcionLexica.class, analizador::proximoToken);

        assertEquals(7, primero.getColumna());
        assertEquals(17, segundo.getColumna());
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
