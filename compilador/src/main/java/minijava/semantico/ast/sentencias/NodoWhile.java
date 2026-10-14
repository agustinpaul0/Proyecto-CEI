package minijava.semantico.ast.sentencias;

import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.errores.ExcepcionSemantica;

// Documento §12.4.
public final class NodoWhile extends NodoSentencia {

    private final NodoExpresion condicion;
    private final NodoSentencia cuerpo;

    public NodoWhile(NodoExpresion condicion, NodoSentencia cuerpo) {
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        chequearCondicion(condicion, contexto);
        chequearCuerpo(cuerpo, contexto);
    }

    @Override
    public String toString() {
        return "(while " + condicion + " " + cuerpo + ")";
    }
}
