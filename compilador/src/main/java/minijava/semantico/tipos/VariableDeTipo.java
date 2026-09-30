package minijava.semantico.tipos;

import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.TipoDeclarado;
import minijava.semantico.errores.ExcepcionSemantica;

public record VariableDeTipo(Token token) implements Tipo {

    @Override
    public String nombre() {
        return token.lexema();
    }

    @Override
    public void verificarEn(TipoDeclarado contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
        if (!contexto.declaraParametroDeTipo(nombre())) {
            throw new ExcepcionSemantica(token, "El parametro de tipo " + nombre()
                    + " no esta declarado en " + contexto.nombre());
        }
        if (enContextoEstatico) {
            throw new ExcepcionSemantica(token, "El parametro de tipo " + nombre()
                    + " no se puede usar en un metodo estatico");
        }
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return sustitucion.reemplazoDe(nombre()).orElse(this);
    }
}
