package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Atributo;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

import java.util.Optional;

// Un nombre sin receptor. Documento §10.1: se busca una local visible, después
// un parámetro, y por último un atributo de la clase, incluidos los heredados.
public final class NodoVariable extends NodoExpresion {

    private final Token nombre;

    public NodoVariable(Token nombre) {
        this.nombre = nombre;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Optional<Tipo> localOParametro = contexto.localOParametro(nombre.lexema());
        if (localOParametro.isPresent()) {
            return localOParametro.get();
        }
        Optional<Atributo> atributo = contexto.clase().atributo(nombre.lexema());
        if (atributo.isEmpty()) {
            throw new ExcepcionSemantica(nombre, "La variable " + nombre.lexema() + " no esta declarada");
        }
        if (contexto.esEstatico()) {
            throw new ExcepcionSemantica(nombre, "El atributo " + nombre.lexema()
                    + " no se puede usar sin receptor en un metodo estatico");
        }
        return atributo.get().tipo();
    }

    @Override
    public Token token() {
        return nombre;
    }

    @Override
    public boolean esAsignable() {
        return true;
    }

    @Override
    public String toString() {
        return nombre.lexema();
    }
}
