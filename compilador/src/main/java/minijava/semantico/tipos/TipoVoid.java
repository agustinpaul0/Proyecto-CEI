package minijava.semantico.tipos;

import minijava.semantico.entidades.ClaseOInterfaz;

public enum TipoVoid implements Tipo {
    VOID;

    @Override
    public String nombre() {
        return "void";
    }

    @Override
    public void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) {
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return this;
    }

    // Documento §9.5: una llamada void no produce un valor que se pueda usar.
    @Override
    public boolean conformaCon(Tipo destino) {
        return false;
    }
}
