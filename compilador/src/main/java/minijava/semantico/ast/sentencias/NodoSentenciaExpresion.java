package minijava.semantico.ast.sentencias;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.ast.expresiones.NodoParentizada;
import minijava.semantico.errores.ExcepcionSemantica;

// `expresion;`. Documento §12.2: la gramática acepta cualquier expresión seguida
// de punto y coma, pero como sentencia sólo vale una asignación o una llamada.
// Documento §11, 1.er párrafo: la expresión se chequea antes de mirar su forma.
public final class NodoSentenciaExpresion extends NodoSentencia {

    private final NodoExpresion expresion;

    public NodoSentenciaExpresion(NodoExpresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        expresion.chequear(contexto);
        if (!expresion.esAsignacion() && !expresion.esLlamada()) {
            Token culpable = expresion instanceof NodoParentizada parentizada
                    ? parentizada.parentesis() : expresion.token();
            throw new ExcepcionSemantica(culpable,
                    "Como sentencia, una expresion tiene que ser una asignacion o una llamada");
        }
    }

    @Override
    public String toString() {
        return "(sentencia " + expresion + ")";
    }
}
