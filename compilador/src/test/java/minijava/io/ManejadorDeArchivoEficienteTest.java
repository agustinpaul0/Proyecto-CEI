package minijava.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

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
    // '\n' es el último devuelto la línea sigue siendo la suya. El léxico depende
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
}
