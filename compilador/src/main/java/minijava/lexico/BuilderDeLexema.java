package minijava.lexico;

import minijava.io.ManejadorDeArchivo;

import java.io.IOException;
import java.io.UncheckedIOException;

// El lexema en curso, el lookahead y las dos fotos de posición: un solo objeto
// porque todo eso cambia junto al consumir un carácter.
public final class BuilderDeLexema {

    private final ManejadorDeArchivo manejador;
    private final StringBuilder lexema = new StringBuilder();
    private char caracterActual;
    // Propia y no manejador.getLineNumber(): esa se adelanta al DEVOLVER el '\n',
    // y un estado que todavía lo mira reportaría la línea siguiente.
    private int linea = 1;
    // La usan los errores que hablan de la construcción entera.
    private PosicionLexica posicionPrimerConsumido;
    // No el lookahead: la usan los errores cuyo ofensivo ya se consumió.
    private PosicionLexica posicionUltimoConsumido;

    public BuilderDeLexema(ManejadorDeArchivo manejador) {
        this.manejador = manejador;
        this.caracterActual = leerSiguienteCaracter();
    }

    public char getCaracterActual() {
        return caracterActual;
    }

    public int getLinea() {
        return linea;
    }

    public void avanzar() {
        PosicionLexica posicion = new PosicionLexica(linea, manejador.getColumnNumber(),
                manejador.getCurrentLineText());
        if (lexema.isEmpty()) {
            posicionPrimerConsumido = posicion;
        }
        lexema.append(caracterActual);
        posicionUltimoConsumido = posicion;
        consumirCaracterActual();
    }

    public void descartar() {
        consumirCaracterActual();
    }

    // EstadoBarra la llama a mitad de token: por eso el reset vive acá.
    public void iniciarNuevoToken() {
        lexema.setLength(0);
        posicionPrimerConsumido = null;
    }

    public String getLexema() {
        return lexema.toString();
    }

    public PosicionLexica posicionPrimerConsumido() {
        return posicionPrimerConsumido;
    }

    public PosicionLexica posicionUltimoConsumido() {
        return posicionUltimoConsumido;
    }

    private void consumirCaracterActual() {
        if (caracterActual == '\n') {
            linea++;
        }
        caracterActual = leerSiguienteCaracter();
    }

    private char leerSiguienteCaracter() {
        try {
            return manejador.getNextChar();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
