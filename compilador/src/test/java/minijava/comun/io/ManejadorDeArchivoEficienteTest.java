package minijava.comun.io;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Tag("comun")
class ManejadorDeArchivoEficienteTest {

    private final ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();

    private Path escribir(Path dir, String nombre, String contenido) throws IOException {
        Path archivo = dir.resolve(nombre);
        Files.writeString(archivo, contenido);
        return archivo;
    }

    @Test
    void devuelveElPrimerCaracterDelArchivo(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "AB").toString());
        assertEquals('A', manejador.getNextChar());
    }

    @Test
    void devuelveLosCaracteresEnOrden(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "AB").toString());
        manejador.getNextChar();
        assertEquals('B', manejador.getNextChar());
    }

    @Test
    void lineaArrancaEnUno(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A").toString());
        assertEquals(1, manejador.getLineNumber());
    }

    // getLineNumber() describe el carácter DEVUELTO, no el próximo: mientras el
    // '\n' es el último devuelto la línea es la suya. El léxico depende
    // de eso — un estado que mira el '\n' como lookahead reporta su propia línea.
    @Test
    void elSaltoDeLineaTodaviaPerteneceALaLineaQueTermina(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\nB").toString());
        manejador.getNextChar();
        assertEquals('\n', manejador.getNextChar());
        assertEquals(1, manejador.getLineNumber());
    }

    @Test
    void saltoDeLineaLfAvanzaLaLinea(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\nB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals('B', manejador.getNextChar());
        assertEquals(2, manejador.getLineNumber());
    }

    @Test
    void crLfCuentaComoUnSoloSalto(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\r\nB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals('B', manejador.getNextChar());
        assertEquals(2, manejador.getLineNumber());
    }

    @Test
    void crSoloCuentaComoSalto(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\rB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals('B', manejador.getNextChar());
        assertEquals(2, manejador.getLineNumber());
    }

    // La invariante que hace innecesario el contador propio que tenía
    // BuilderDeLexema: los tres accesores describen SIEMPRE el mismo carácter.
    @Test
    void losTresDatosDePosicionDescribenElMismoCaracter(@TempDir Path dir) throws IOException {
        String fuente = "uno\ndos dos\n\ncuatro";
        manejador.open(escribir(dir, "f.txt", fuente).toString());
        String[] lineas = fuente.split("\n", -1);

        char c;
        while ((c = manejador.getNextChar()) != SourceManager.END_OF_FILE) {
            int linea = manejador.getLineNumber();
            String textoDeLaLinea = lineas[linea - 1];
            assertEquals(textoDeLaLinea, manejador.getCurrentLineText(),
                    "la línea fuente no es la de getLineNumber()");
            int columna = manejador.getColumnNumber();
            char esperado = columna > textoDeLaLinea.length() ? '\n' : textoDeLaLinea.charAt(columna - 1);
            assertEquals(esperado, c,
                    "el carácter devuelto no es el de (linea " + linea + ", columna " + columna + ")");
        }
    }

    @Test
    void eofEnArchivoQueTerminaConVeintiunSaltosDeLinea(@TempDir Path dir) throws IOException {
        String contenido = "x".repeat(1) + "\n".repeat(21);
        manejador.open(escribir(dir, "f.txt", contenido).toString());
        char c;
        do {
            c = manejador.getNextChar();
        } while (c != SourceManager.END_OF_FILE);
        assertEquals(22, manejador.getLineNumber());
    }

    @Test
    void eofEnArchivoDeDoceSaltosSinNewlineFinal(@TempDir Path dir) throws IOException {
        String contenido = "\n".repeat(12) + "x";
        manejador.open(escribir(dir, "f.txt", contenido).toString());
        char c;
        do {
            c = manejador.getNextChar();
        } while (c != SourceManager.END_OF_FILE);
        assertEquals(13, manejador.getLineNumber());
    }

    @Test
    void eofEnArchivoDeDoceSaltosConNewlineFinal(@TempDir Path dir) throws IOException {
        String contenido = "\n".repeat(12);
        manejador.open(escribir(dir, "f.txt", contenido).toString());
        char c;
        do {
            c = manejador.getNextChar();
        } while (c != SourceManager.END_OF_FILE);
        assertEquals(13, manejador.getLineNumber());
    }

    @Test
    void archivoVacioDaEofSinExplotar(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "").toString());
        assertEquals(SourceManager.END_OF_FILE, manejador.getNextChar());
        assertEquals(1, manejador.getLineNumber());
    }

    @Test
    void archivoInexistenteLanzaFileNotFoundException(@TempDir Path dir) {
        assertThrows(FileNotFoundException.class,
                () -> manejador.open(dir.resolve("no-existe.txt").toString()));
    }

    @Test
    void columnaDelPrimerCaracterEsUno(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "AB").toString());
        manejador.getNextChar();
        assertEquals(1, manejador.getColumnNumber());
    }

    @Test
    void columnaAvanzaConCadaCaracter(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "AB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals(2, manejador.getColumnNumber());
    }

    @Test
    void columnaSeReiniciaTrasSaltoDeLinea(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "AB\nC").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals(1, manejador.getColumnNumber());
    }

    @Test
    void columnaApuntaAlCaracterOfensivoNoAlSiguiente(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "v1 + # chau").toString());
        for (int i = 0; i < 6; i++) {
            manejador.getNextChar();
        }
        assertEquals(6, manejador.getColumnNumber());
    }

    @Test
    void textoDeLaLineaActualEsLaLineaCompleta(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "v1 + # chau\nif class}").toString());
        for (int i = 0; i < 6; i++) {
            manejador.getNextChar();
        }
        assertEquals("v1 + # chau", manejador.getCurrentLineText());
    }

    @Test
    void textoDeLaLineaActualCambiaEnLaSegundaLinea(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "v1\nchau").toString());
        for (int i = 0; i < 5; i++) {
            manejador.getNextChar();
        }
        assertEquals("chau", manejador.getCurrentLineText());
    }

    // Regresión: al DEVOLVER el '\n' que termina una línea, getCurrentLineText()
    // debe seguir mostrando ESA línea y no la siguiente. Con un char sin cerrar al
    // final de línea, el '^' señalaba texto sin relación con el error.
    @Test
    void textoDeLaLineaSigueSiendoLaAnteriorJustoAlDevolverElSalto(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "v1\nchau").toString());
        char ultimo = ' ';
        for (int i = 0; i < 3; i++) {
            ultimo = manejador.getNextChar();
        }
        assertEquals('\n', ultimo);
        assertEquals("v1", manejador.getCurrentLineText());
    }

    @Test
    void primeraLineaVaciaReportaTextoVacioNoLaSegundaLinea(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "\nsegunda").toString());
        char primero = manejador.getNextChar();

        assertEquals('\n', primero);
        assertEquals("", manejador.getCurrentLineText());
    }

    // --- Costo de ubicar el error (logro Manejador de Archivos Eficiente) ---
    //
    // La cátedra pidió que informar la columna del error no obligue a releer el
    // archivo. Acá no hay ni relectura ni buffer de últimas N líneas: la columna y
    // el inicio de la línea actual son contadores que getNextChar() actualiza, así
    // que getColumnNumber() es O(1) y getCurrentLineText() recorre sólo la línea
    // actual. Estos tests fijan esa propiedad donde más se notaría: el error en el
    // último carácter de un archivo grande.

    @Test
    void ubicarElErrorEnLaUltimaLineaDeUnArchivoGrandeNoDependeDelTamanoDelArchivo(
            @TempDir Path dir) throws IOException {
        int lineas = 200_000;
        StringBuilder fuente = new StringBuilder();
        for (int i = 0; i < lineas; i++) {
            fuente.append("var v").append(i).append(" = ").append(i).append(";\n");
        }
        fuente.append("var ultima = 1;");
        Path archivo = escribir(dir, "grande.mjava", fuente.toString());

        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            manejador.open(archivo.toString());
            while (manejador.getNextChar() != SourceManager.END_OF_FILE) {
                // Recorrer entero: interesa el estado al llegar al final.
            }
            assertEquals(lineas + 1, manejador.getLineNumber());
            assertEquals("var ultima = 1;", manejador.getCurrentLineText());
        });
    }

    // Una sola línea enorme: getCurrentLineText() es O(largo de la línea), y ese
    // largo es el del texto que el reporte va a imprimir de todos modos.
    @Test
    void unaLineaDeUnMillonDeCaracteresSeDevuelveEntera(@TempDir Path dir) throws IOException {
        String linea = "a".repeat(1_000_000);
        manejador.open(escribir(dir, "larga.mjava", linea).toString());

        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            manejador.getNextChar();
            assertEquals(1, manejador.getColumnNumber());
            assertEquals(linea.length(), manejador.getCurrentLineText().length());
        });
    }

    // Aspectos Léxicos §3.1: LF, CR y CRLF son cada uno UN salto de línea, y dos
    // fuentes que difieren sólo en eso tienen que dar los mismos números de línea.
    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r", "\r\n"})
    void losTresFinalesDeLineaDanLaMismaNumeracion(String finDeLinea, @TempDir Path dir)
            throws IOException {
        manejador.open(escribir(dir, "f.txt",
                "uno" + finDeLinea + "dos" + finDeLinea + "tres").toString());

        int linea = 1;
        char leido;
        StringBuilder ultimaLinea = new StringBuilder();
        while ((leido = manejador.getNextChar()) != SourceManager.END_OF_FILE) {
            if (leido == '\n') {
                linea++;
                ultimaLinea.setLength(0);
            } else {
                ultimaLinea.append(leido);
            }
            // Nunca aparece un '\r' crudo: los tres se normalizan a un solo '\n'.
            assertEquals(false, leido == '\r', "quedó un retorno de carro sin normalizar");
        }
        assertEquals(3, linea);
        assertEquals(3, manejador.getLineNumber());
        assertEquals("tres", ultimaLinea.toString());
    }
}
