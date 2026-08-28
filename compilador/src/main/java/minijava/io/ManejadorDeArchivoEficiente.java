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
    private int lineaDevuelta;
    private int inicioDeLineaActual;
    private int inicioDeLineaDevuelta;
    private int columnaSiguiente;
    private int columnaDevuelta;
    private boolean devolvioFinDeArchivo;

    @Override
    public void open(String filePath) throws FileNotFoundException {
        try {
            // El IDE sugiere Files.readString: NO aplicar. Ante bytes UTF-8 inválidos
            // ésa lanza MalformedInputException (verificado) y esto los mapea a U+FFFD:
            // un binario tiene que dar errores léxicos, no matar el compilador.
            byte[] bytes = Files.readAllBytes(Path.of(filePath));
            String texto = new String(bytes, StandardCharsets.UTF_8);
            contenido = texto.replace("\r\n", "\n").replace('\r', '\n').toCharArray();
        } catch (NoSuchFileException excepcion) {
            throw new FileNotFoundException(filePath);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
        posicion = 0;
        devolvioFinDeArchivo = false;
        numeroDeLinea = 1;
        lineaDevuelta = 1;
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
            fotografiarPosicion();
            devolvioFinDeArchivo = true;
            return SourceManager.END_OF_FILE;
        }
        char actual = contenido[posicion++];
        // Antes de mutar: si no, los tres datos describen la línea SIGUIENTE justo
        // al devolver el '\n' que termina la actual.
        fotografiarPosicion();
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

    // Los tres accesores describen el mismo carácter: el último devuelto, que es
    // el lookahead del léxico. Mezclarlos deja el '^' señalando texto ajeno.
    @Override
    public int getLineNumber() {
        return lineaDevuelta;
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

    private void fotografiarPosicion() {
        lineaDevuelta = numeroDeLinea;
        columnaDevuelta = columnaSiguiente;
        inicioDeLineaDevuelta = inicioDeLineaActual;
    }
}
