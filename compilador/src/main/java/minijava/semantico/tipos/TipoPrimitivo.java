package minijava.semantico.tipos;

import minijava.semantico.entidades.TipoDeclarado;

// `float` es tipo primitivo por el logro de la Etapa 1 que confirmó el profesor.
public enum TipoPrimitivo implements Tipo {
    BOOLEAN("boolean"),
    CHAR("char"),
    INT("int"),
    FLOAT("float");

    private final String nombre;

    TipoPrimitivo(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public void verificarEn(TipoDeclarado contexto, boolean enContextoEstatico) {
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return this;
    }
}
