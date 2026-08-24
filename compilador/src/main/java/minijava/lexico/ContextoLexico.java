package minijava.lexico;

import minijava.errores.ExcepcionLexica;
import minijava.io.ManejadorDeArchivo;
import minijava.io.SourceManager;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ContextoLexico {

    private final ManejadorDeArchivo manejador;
    private final StringBuilder lexema = new StringBuilder();
    private char caracterActual;
    // Línea propia, no delegada a manejador.getLineNumber(): esa cuenta se
    // adelanta en el momento en que un '\n' es DEVUELTO como lookahead, no
    // cuando efectivamente se lo consume. Acá se incrementa recién al
    // consumir (avanzar/descartar) el '\n', para que un estado que todavía
    // está MIRANDO el '\n' (sin haberlo consumido) reporte la línea que ese
    // salto termina, no la siguiente.
    private int linea = 1;

    public ContextoLexico(ManejadorDeArchivo manejador) {
        this.manejador = manejador;
        this.caracterActual = leerSiguienteCaracter();
    }

    // ManejadorDeArchivoEficiente nunca lanza IOException en la práctica;
    // se adapta acá para no propagar el checked exception de la interfaz.
    private char leerSiguienteCaracter() {
        try {
            return manejador.getNextChar();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    public char getCaracterActual() {
        return caracterActual;
    }

    public boolean esFinDeArchivo() {
        return caracterActual == SourceManager.END_OF_FILE;
    }

    // Consume el carácter actual: lo agrega al lexema y avanza la lectura.
    public void avanzar() {
        lexema.append(caracterActual);
        consumirCaracterActual();
    }

    // Descarta el carácter actual sin agregarlo al lexema (blancos, comentarios).
    public void descartar() {
        consumirCaracterActual();
    }

    private void consumirCaracterActual() {
        if (caracterActual == '\n') {
            linea++;
        }
        caracterActual = leerSiguienteCaracter();
    }

    // Agrega el carácter ofensivo al lexema SIN avanzar (para el reporte de error).
    public void agregarCaracterOfensivo() {
        lexema.append(caracterActual);
    }

    public void iniciarNuevoToken() {
        lexema.setLength(0);
    }

    public String getLexema() {
        return lexema.toString();
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return manejador.getColumnNumber();
    }

    public String getLineaFuente() {
        return manejador.getCurrentLineText();
    }

    // Punto único de construcción de ExcepcionLexica: garantiza que todo
    // error lleve consistentemente lexema, línea, columna y la línea fuente
    // completa (necesaria para el Reporte de Error Elegante).
    public ExcepcionLexica crearExcepcionLexica() {
        return new ExcepcionLexica(getLexema(), getLinea(), getColumna(), getLineaFuente());
    }

    // Variante para errores cuyo lexema/línea reportados no son los del
    // lookahead actual (p. ej. comentario multilínea sin cerrar: se reporta
    // la línea de APERTURA, no la línea donde se encontró el EOF).
    public ExcepcionLexica crearExcepcionLexica(String lexema, int linea) {
        return new ExcepcionLexica(lexema, linea, getColumna(), getLineaFuente());
    }
}
