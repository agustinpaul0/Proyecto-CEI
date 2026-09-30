package minijava.semantico.tipos;

import minijava.semantico.entidades.TipoDeclarado;
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
    public void verificarEn(TipoDeclarado contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
        base.verificarEn(contexto, enContextoEstatico);
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return new TipoArreglo(base.instanciar(sustitucion), dimensiones);
    }
}
