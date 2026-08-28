package minijava.errores;

public final class ExcepcionLexica extends Exception {

    private final TipoDeErrorLexico tipo;
    private final String lexema;
    private final PosicionLexica posicion;
    // La parte concretamente ofensiva, cuando el lexema armado que exige Pautas
    // es más grande que ella. Null salvo en el escape mal formado de litString.
    private final String fragmentoOfensivo;

    public ExcepcionLexica(TipoDeErrorLexico tipo, String lexema, PosicionLexica posicion) {
        this(tipo, lexema, posicion, null);
    }

    public ExcepcionLexica(TipoDeErrorLexico tipo, String lexema, PosicionLexica posicion,
                           String fragmentoOfensivo) {
        super("Error léxico en línea " + posicion.linea() + ": "
                + tipo.describir(lexema, fragmentoOfensivo));
        this.tipo = tipo;
        this.lexema = lexema;
        this.posicion = posicion;
        this.fragmentoOfensivo = fragmentoOfensivo;
    }

    public TipoDeErrorLexico getTipo() {
        return tipo;
    }

    public String getLexemaOfensivo() {
        return lexema;
    }

    public String getFragmentoOfensivo() {
        return fragmentoOfensivo;
    }

    public PosicionLexica getPosicion() {
        return posicion;
    }

    public int getLinea() {
        return posicion.linea();
    }

    public int getColumna() {
        return posicion.columna();
    }

    public String getLineaFuente() {
        return posicion.lineaFuente();
    }
}
