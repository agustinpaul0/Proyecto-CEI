package minijava.semantico.ast.sentencias;

import minijava.generacion.Generador;
import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.errores.ExcepcionSemantica;

// Documento §12.4. `sino` es null cuando no hay else.
public final class NodoIf extends NodoSentencia {

    private final NodoExpresion condicion;
    private final NodoSentencia entonces;
    private final NodoSentencia sino;

    public NodoIf(NodoExpresion condicion, NodoSentencia entonces, NodoSentencia sino) {
        this.condicion = condicion;
        this.entonces = entonces;
        this.sino = sino;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        chequearCondicion(condicion, contexto);
        chequearCuerpo(entonces, contexto);
        if (sino != null) {
            chequearCuerpo(sino, contexto);
        }
    }

    // Apunte §5.
    @Override
    public void generar(Generador g) {
        String fin = g.etiquetaNueva("fin_if");
        condicion.generar(g);
        if (sino == null) {
            g.gen("BF " + fin);
            entonces.generar(g);
        } else {
            String rama = g.etiquetaNueva("else");
            g.gen("BF " + rama);
            entonces.generar(g);
            g.gen("JUMP " + fin);
            g.etiqueta(rama);
            sino.generar(g);
        }
        g.etiqueta(fin);
    }

    @Override
    public String toString() {
        return "(if " + condicion + " " + entonces + (sino == null ? "" : " " + sino) + ")";
    }
}
