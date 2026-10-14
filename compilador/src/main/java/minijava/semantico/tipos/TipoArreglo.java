package minijava.semantico.tipos;

import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.errores.ExcepcionSemantica;

public record TipoArreglo(Tipo base, int dimensiones) implements Tipo {

    public static Tipo de(Tipo base, int dimensiones) {
        return dimensiones == 0 ? base : new TipoArreglo(base, dimensiones);
    }

    @Override
    public String nombre() {
        return base.nombre() + "[]".repeat(dimensiones);
    }

    @Override
    public String identidad() {
        return base.identidad() + "[]".repeat(dimensiones);
    }

    @Override
    public void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
        base.verificarEn(contexto, enContextoEstatico);
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return new TipoArreglo(base.instanciar(sustitucion), dimensiones);
    }

    // Documento §9.4: los arreglos son invariantes, y todos conforman con Object.
    @Override
    public boolean conformaCon(Tipo destino) {
        return esIgualA(destino) || destino instanceof TipoReferencia referencia && referencia.esObject();
    }

    // Cada indexación saca una dimensión (documento §11.8).
    public Tipo componente() {
        return de(base, dimensiones - 1);
    }
}
