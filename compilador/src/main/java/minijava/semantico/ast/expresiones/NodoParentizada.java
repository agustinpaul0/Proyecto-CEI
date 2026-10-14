package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

// Documento §11.4: conserva el tipo de la expresión de adentro y nada más. No es
// asignable, ni asignación, ni llamada.
public final class NodoParentizada extends NodoExpresion {

    private final Token parentesis;
    private final NodoExpresion interna;

    public NodoParentizada(Token parentesis, NodoExpresion interna) {
        this.parentesis = parentesis;
        this.interna = interna;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        return interna.chequear(contexto);
    }

    // Como en javac, un error de tipo de la expresión entera se ubica adentro de
    // los paréntesis; el de una sentencia que no es asignación ni llamada, en el `(`.
    @Override
    public Token token() {
        return interna.token();
    }

    public Token parentesis() {
        return parentesis;
    }

    @Override
    public boolean empiezaConParentesis() {
        return true;
    }

    @Override
    public String toString() {
        return "(paren " + interna + ")";
    }
}
