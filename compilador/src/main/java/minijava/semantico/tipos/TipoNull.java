package minijava.semantico.tipos;

import minijava.semantico.entidades.ClaseOInterfaz;

// El tipo especial del literal `null`, que no se puede escribir en una
// declaración (documento §3 y §9.5).
public enum TipoNull implements Tipo {
    NULL;

    @Override
    public String nombre() {
        return "null";
    }

    @Override
    public void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) {
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return this;
    }

    @Override
    public boolean conformaCon(Tipo destino) {
        return switch (destino) {
            case TipoReferencia referencia -> true;
            case VariableDeTipo parametro -> true;
            case TipoArreglo arreglo -> true;
            case TipoNull nulo -> true;
            case TipoPrimitivo primitivo -> false;
            case TipoVoid vacio -> false;
        };
    }
}
