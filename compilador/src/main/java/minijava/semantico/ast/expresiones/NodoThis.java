package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

// Documento §11.3 y §10.4: vale sólo en constructores y métodos de instancia.
public final class NodoThis extends NodoExpresion {

    private final Token palabraThis;

    public NodoThis(Token palabraThis) {
        this.palabraThis = palabraThis;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        if (contexto.esEstatico()) {
            throw new ExcepcionSemantica(palabraThis, "this no se puede usar en un metodo estatico");
        }
        return contexto.tipoDeThis();
    }

    @Override
    public Token token() {
        return palabraThis;
    }

    @Override
    public String toString() {
        return "this";
    }
}
