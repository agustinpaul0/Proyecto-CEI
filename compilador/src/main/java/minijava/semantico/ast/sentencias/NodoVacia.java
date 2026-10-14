package minijava.semantico.ast.sentencias;

import minijava.semantico.ast.Contexto;

// Documento §12.1: `;` es correcta por sí misma.
public final class NodoVacia extends NodoSentencia {

    @Override
    public void chequear(Contexto contexto) {
    }

    @Override
    public String toString() {
        return ";";
    }
}
