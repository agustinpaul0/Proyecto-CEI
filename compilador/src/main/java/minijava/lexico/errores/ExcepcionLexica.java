package minijava.lexico.errores;

public final class ExcepcionLexica extends Exception {

    private final TipoDeErrorLexico tipo;
    private final String lexema;
    private final PosicionLexica posicion;

    public ExcepcionLexica(TipoDeErrorLexico tipo, String lexema, PosicionLexica posicion) {
        super("Error léxico en línea " + posicion.linea() + ": " + tipo.describir(lexema));
        this.tipo = tipo;
        this.lexema = lexema;
        this.posicion = posicion;
    }

    public TipoDeErrorLexico getTipo() {
        return tipo;
    }

    public String getLexemaOfensivo() {
        return lexema;
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
