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

    @Test
    void saltoDeLineaLfAvanzaLaLinea(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\nB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals(2, manejador.getLineNumber());
    }

    @Test
    void crLfCuentaComoUnSoloSalto(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\r\nB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals(2, manejador.getLineNumber());
    }

    @Test
    void crSoloCuentaComoSalto(@TempDir Path dir) throws IOException {
        manejador.open(escribir(dir, "f.txt", "A\rB").toString());
        manejador.getNextChar();
        manejador.getNextChar();
        assertEquals(2, manejador.getLineNumber());
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

    // Regresión: justo cuando getNextChar() DEVUELVE el '\n' que termina una
    // línea, getCurrentLineText() debe seguir mostrando ESA línea (la que el
    // salto termina), no la siguiente — aunque el salto ya haya sido
    // devuelto. Encontrado construyendo un caso de corpus con un char literal
    // sin cerrar al final de línea: el reporte mostraba la línea siguiente y
    // el '^' señalaba texto sin relación con el error.
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
