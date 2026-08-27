package minijava.io;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

// Logro "Manejador de Archivos Eficiente": el fuente se carga entero y se sirve
// carácter a carácter. Por qué el de la cátedra no alcanza: documentacion/manejador-de-archivo.md.
public final class ManejadorDeArchivoEficiente implements ManejadorDeArchivo {

    private char[] contenido;
    private int posicion;
    private int numeroDeLinea;
    private int inicioDeLineaActual;
    private int inicioDeLineaDevuelta;
    private int columnaSiguiente;
    private int columnaDevuelta;
    private boolean devolvioFinDeArchivo;

    @Override
    public void open(String filePath) throws FileNotFoundException {
        try {
            // new String(bytes, UTF_8) mapea lo mal formado a U+FFFD en vez de
            // lanzar como Files.readString: un binario da errores léxicos, no un crash.
            String texto = new String(Files.readAllBytes(Path.of(filePath)), StandardCharsets.UTF_8);
            contenido = texto.replace("\r\n", "\n").replace('\r', '\n').toCharArray();
        } catch (NoSuchFileException excepcion) {
            throw new FileNotFoundException(filePath);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
        posicion = 0;
        devolvioFinDeArchivo = false;
        numeroDeLinea = 1;
        inicioDeLineaActual = 0;
        columnaSiguiente = 1;
    }

    @Override
    public void close() {
        contenido = null;
    }

    @Override
    public char getNextChar() {
        if (posicion >= contenido.length) {
            columnaDevuelta = columnaSiguiente;
            inicioDeLineaDevuelta = inicioDeLineaActual;
            devolvioFinDeArchivo = true;
            return SourceManager.END_OF_FILE;
        }
        char actual = contenido[posicion++];
        columnaDevuelta = columnaSiguiente;
        // Antes de mutar inicioDeLineaActual: si no, getCurrentLineText() muestra
        // la línea siguiente justo al devolver el '\n' que termina la actual.
        inicioDeLineaDevuelta = inicioDeLineaActual;
        if (actual == '\n') {
            numeroDeLinea++;
            columnaSiguiente = 1;
            inicioDeLineaActual = posicion;
        } else {
            columnaSiguiente++;
        }
        return actual;
    }

    @Override
    public boolean estaEnFinDeArchivo() {
        return devolvioFinDeArchivo;
    }

    @Override
    public int getLineNumber() {
        return numeroDeLinea;
    }

    @Override
    public int getColumnNumber() {
        return columnaDevuelta;
    }

    @Override
    public String getCurrentLineText() {
        int fin = inicioDeLineaDevuelta;
        while (fin < contenido.length && contenido[fin] != '\n') {
            fin++;
        }
        return new String(contenido, inicioDeLineaDevuelta, fin - inicioDeLineaDevuelta);
    }
}
