package minijava.lexico;

import minijava.io.ManejadorDeArchivo;

import java.io.IOException;
import java.io.UncheckedIOException;

// El lexema en construcción, el carácter que se está mirando, y las dos fotos
// de posición que los errores necesitan. Un solo objeto porque los cuatro
// datos cambian juntos: no se puede consumir un carácter sin actualizar el
// lexema, la línea y la posición del último consumido.
public final class BuilderDeLexema {

    private final ManejadorDeArchivo manejador;
    private final StringBuilder lexema = new StringBuilder();
    private char caracterActual;
    // Línea propia, no delegada a manejador.getLineNumber(): esa cuenta se
    // adelanta cuando un '\n' es DEVUELTO como lookahead, no cuando se lo
    // consume. Acá se incrementa recién al consumirlo, para que un estado que
    // todavía está MIRANDO el '\n' reporte la línea que ese salto termina.
    private int linea = 1;
    // Posición del PRIMER carácter del lexema en curso. La usan los errores
    // cuyo mensaje habla de la construcción entera.
    private PosicionLexica posicionPrimerConsumido;
    // Posición del último carácter agregado al lexema, no del lookahead (que
    // ya puede haber avanzado más allá). La usan los errores cuyo carácter
    // ofensivo se consumió antes del throw.
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

    // EstadoBarra la llama a mitad de token para descartar el '/' de un
    // comentario, así que el reset de la posición de apertura tiene que vivir
    // acá y no en AnalizadorLexico.proximoToken().
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

    // ManejadorDeArchivoEficiente nunca lanza IOException en la práctica;
    // se adapta acá para no propagar el checked exception de la interfaz.
    private char leerSiguienteCaracter() {
        try {
            return manejador.getNextChar();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
