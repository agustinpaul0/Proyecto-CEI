package minijava.errores;

public final class ExcepcionLexica extends Exception {

    private final String lexema;
    private final int linea;
    private final int columna;
    private final String lineaFuente;

    public ExcepcionLexica(String lexema, int linea, int columna, String lineaFuente) {
        super("Error léxico en línea " + linea + ": '" + lexema + "' no es válido");
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.lineaFuente = lineaFuente;
    }

    public String getLineaFuente() {
        return lineaFuente;
    }

    public String getLexemaOfensivo() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }
}
