package minijava.semantico.tipos;

import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.errores.ExcepcionSemantica;

// `declarante` es el nombre de la clase o interfaz donde se escribió la `T`.
public record VariableDeTipo(Token token, String declarante) implements Tipo {

    @Override
    public String nombre() {
        return token.lexema();
    }

    @Override
    public String identidad() {
        return nombre() + "@" + declarante;
    }

    @Override
    public void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
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
        return sustitucion.reemplazoDe(this).orElse(this);
    }

    // Documento §9.3: sin cotas, ni siquiera Object; sólo conforma consigo mismo.
    @Override
    public boolean conformaCon(Tipo destino) {
        return esIgualA(destino);
    }
}
