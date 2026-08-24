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
    // Posición del último carácter agregado al lexema (no del lookahead
    // actual, que puede ya haber avanzado más allá). Se actualiza dentro de
    // avanzar(), justo antes de consumir, cuando línea/columna/línea-fuente
    // todavía describen ese carácter. La usan los estados cuyo error no es
    // "este carácter que estoy mirando es inválido" sino "el lexema que ya
    // terminé de construir resultó inválido" (p. ej. un operador con
    // segundo carácter obligatorio ausente, o un entero de más de 9
    // dígitos): en esos casos el lookahead actual no tiene ninguna relación
    // con el motivo del error, así que reportarlo sería engañoso.
    private PosicionLexica posicionUltimoConsumido;

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

    public void avanzar() {
        lexema.append(caracterActual);
        posicionUltimoConsumido = new PosicionLexica(getLinea(), getColumna(), getLineaFuente());
        consumirCaracterActual();
    }

    public PosicionLexica posicionUltimoConsumido() {
        return posicionUltimoConsumido;
    }

    public void descartar() {
        consumirCaracterActual();
    }

    private void consumirCaracterActual() {
        if (caracterActual == '\n') {
            linea++;
        }
        caracterActual = leerSiguienteCaracter();
    }

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

    public ExcepcionLexica crearExcepcionLexica() {
        return new ExcepcionLexica(getLexema(), getLinea(), getColumna(), getLineaFuente());
    }

    // Variante para errores sobre una posición distinta de la del lookahead
    // actual: el lexema ya construido resultó inválido como un todo (ver
    // posicionUltimoConsumido()), o el error es sobre la posición de
    // APERTURA de una construcción que nunca cerró (comentario multilínea).
    public ExcepcionLexica crearExcepcionLexica(String lexema, PosicionLexica posicion) {
        return new ExcepcionLexica(lexema, posicion.linea(), posicion.columna(), posicion.lineaFuente());
    }
}
