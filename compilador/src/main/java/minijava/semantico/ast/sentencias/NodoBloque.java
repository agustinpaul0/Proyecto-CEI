package minijava.semantico.ast.sentencias;

import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;

import java.util.List;
import java.util.stream.Collectors;

// Documento §12.6: las sentencias se chequean en orden, también después de un
// return, y las locales del bloque dejan de ser visibles al cerrarlo.
public final class NodoBloque extends NodoSentencia {

    private final List<NodoSentencia> sentencias;

    public NodoBloque(List<NodoSentencia> sentencias) {
        this.sentencias = List.copyOf(sentencias);
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        contexto.abrirBloque();
        for (NodoSentencia sentencia : sentencias) {
            sentencia.chequear(contexto);
        }
        contexto.cerrarBloque();
    }

    @Override
    public String toString() {
        return sentencias.stream().map(NodoSentencia::toString).collect(Collectors.joining(" ", "{", "}"));
    }
}
