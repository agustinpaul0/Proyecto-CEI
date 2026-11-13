package minijava.semantico.ast.expresiones;

import minijava.generacion.Generador;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

// Documento §11.9. Los dos errores van en el `=`, como en los casos de la cátedra
// semIIError02 y semIIError03. El orden es el del pseudocódigo de la Clase 11
// (slides 26, 31 y 36): los dos lados, la conformidad y recién después si el lado
// izquierdo es asignable.
public final class NodoAsignacion extends NodoExpresion {

    private final NodoExpresion izquierda;
    private final Token igual;
    private final NodoExpresion derecha;

    public NodoAsignacion(NodoExpresion izquierda, Token igual, NodoExpresion derecha) {
        this.izquierda = izquierda;
        this.igual = igual;
        this.derecha = derecha;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo destino = izquierda.chequear(contexto);
        Tipo origen = derecha.chequear(contexto);
        if (!origen.conformaCon(destino)) {
            throw new ExcepcionSemantica(igual, "En la asignacion, el tipo " + origen.nombre()
                    + " no conforma con " + destino.nombre());
        }
        if (!izquierda.esAsignable()) {
            throw new ExcepcionSemantica(igual, "El lado izquierdo de la asignacion no es asignable");
        }
        return destino;
    }

    // Clase 16, slides 18-29, y Apunte §7: el valor, una copia que queda como
    // resultado de la asignación, y el destino. El lado derecho se evalúa primero,
    // a diferencia de Java (CONVENCIONES-CATEDRA §4).
    @Override
    public void generar(Generador g) {
        derecha.generar(g);
        g.gen("DUP");
        izquierda.generarAlmacenamiento(g);
    }

    @Override
    public Token token() {
        return igual;
    }

    @Override
    public boolean esAsignacion() {
        return true;
    }

    @Override
    public String toString() {
        return "(= " + izquierda + " " + derecha + ")";
    }
}
