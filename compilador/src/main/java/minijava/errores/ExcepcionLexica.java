package minijava.errores;

public final class ExcepcionLexica extends Exception {

    private final TipoDeErrorLexico tipo;
    private final String lexema;
    private final int linea;
    private final int columna;
    private final String lineaFuente;
    // La parte concretamente ofensiva, cuando el lexema armado que exige Pautas
    // es más grande que ella. Null salvo en el escape mal formado de litString.
    private final String fragmentoOfensivo;

    public ExcepcionLexica(TipoDeErrorLexico tipo, String lexema, int linea, int columna,
                           String lineaFuente) {
        this(tipo, lexema, linea, columna, lineaFuente, null);
    }

    public ExcepcionLexica(TipoDeErrorLexico tipo, String lexema, int linea, int columna,
                           String lineaFuente, String fragmentoOfensivo) {
        super("Error léxico en línea " + linea + ": " + tipo.describir(lexema, fragmentoOfensivo));
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.lineaFuente = lineaFuente;
        this.fragmentoOfensivo = fragmentoOfensivo;
    }

    public TipoDeErrorLexico getTipo() {
        return tipo;
    }

    public String getLineaFuente() {
        return lineaFuente;
    }

    public String getLexemaOfensivo() {
        return lexema;
    }

    public String getFragmentoOfensivo() {
        return fragmentoOfensivo;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }
}
