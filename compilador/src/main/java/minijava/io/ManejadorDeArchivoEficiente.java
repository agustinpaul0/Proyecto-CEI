package minijava.io;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

// Logro "Manejador de Archivos Eficiente", y en la práctica un requisito: el
// SourceManagerImpl provisto lee por líneas y reporta el EOF una línea antes de
// lo que esperan los propios casos de ejemplo de la cátedra cuando el archivo
// termina en salto de línea (además de romper con un archivo vacío).
//
// Acá el fuente se carga entero una sola vez y se sirve carácter por carácter.
// Eso deja disponible todo el contexto, que es lo que hace exactos y baratos
// getCurrentLineText() y el reconteo de columnas del Reporte de Error Elegante.
// Ver documentacion/manejador-de-archivo.md.
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
            // new String(bytes, UTF_8) reemplaza las secuencias mal formadas
            // por U+FFFD en vez de lanzar MalformedInputException, que es lo
            // que hace Files.readString: un archivo binario reventaba el
            // compilador con un UncheckedIOException. Ahora los bytes basura
            // llegan al léxico como caracteres desconocidos y se reportan como
            // errores léxicos normales.
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
        // Se captura ANTES de mutar inicioDeLineaActual: mismo patrón que
        // columnaDevuelta/columnaSiguiente. Sin esto, getCurrentLineText()
        // mostraría la línea SIGUIENTE en el instante exacto en que se
        // devuelve el '\n' que termina la línea actual (bug real, encontrado
        // escribiendo el test antes que el código).
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

    // Columna del último carácter DEVUELTO (no del próximo): así el reporte
    // de error apunta a la posición real del carácter ofensivo.
    @Override
    public int getColumnNumber() {
        return columnaDevuelta;
    }

    // El TEXTO de la línea del último carácter devuelto, sin su salto final
    // (mismo criterio que getColumnNumber()). Lo consume el Reporte de Error
    // Elegante para imprimir la línea y poner el '^' debajo.
    @Override
    public String getCurrentLineText() {
        int fin = inicioDeLineaDevuelta;
        while (fin < contenido.length && contenido[fin] != '\n') {
            fin++;
        }
        return new String(contenido, inicioDeLineaDevuelta, fin - inicioDeLineaDevuelta);
    }
}
