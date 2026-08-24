package minijava.io;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class ManejadorDeArchivoEficiente implements ManejadorDeArchivo {

    private char[] contenido;
    private int posicion;
    private int numeroDeLinea;
    private int inicioDeLineaActual;
    private int inicioDeLineaDevuelta;
    private int columnaSiguiente;
    private int columnaDevuelta;

    @Override
    public void open(String filePath) throws FileNotFoundException {
        try {
            String texto = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            contenido = texto.replace("\r\n", "\n").replace('\r', '\n').toCharArray();
        } catch (NoSuchFileException excepcion) {
            throw new FileNotFoundException(filePath);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
        posicion = 0;
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
            return SourceManager.END_OF_FILE;
        }
        char actual = contenido[posicion++];
        columnaDevuelta = columnaSiguiente;
        // Se captura ANTES de mutar inicioDeLineaActual: mismo patrón que
        // columnaDevuelta/columnaSiguiente. Sin esto, getCurrentLineText()
        // mostraría la línea SIGUIENTE en el instante exacto en que se
        // devuelve el '\n' que termina la línea actual (bug real encontrado
        // por TDD, ver bitácora en AGENTS.md).
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
    public int getLineNumber() {
        return numeroDeLinea;
    }

    // Columna del último carácter DEVUELTO (no del próximo): así el reporte
    // de error apunta a la posición real del carácter ofensivo.
    @Override
    public int getColumnNumber() {
        return columnaDevuelta;
    }

    // Línea del último carácter DEVUELTO (mismo criterio que getColumnNumber()).
    @Override
    public String getCurrentLineText() {
        int fin = inicioDeLineaDevuelta;
        while (fin < contenido.length && contenido[fin] != '\n') {
            fin++;
        }
        return new String(contenido, inicioDeLineaDevuelta, fin - inicioDeLineaDevuelta);
    }
}
