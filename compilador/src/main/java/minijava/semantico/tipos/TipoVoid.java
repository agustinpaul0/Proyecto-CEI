package minijava.semantico.tipos;

import minijava.semantico.entidades.TipoDeclarado;

public enum TipoVoid implements Tipo {
    VOID;

    @Override
    public String nombre() {
        return "void";
    }

    @Override
    public void verificarEn(TipoDeclarado contexto, boolean enContextoEstatico) {
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return this;
    }
}
