package minijava.semantico.ast.sentencias;

import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoPrimitivo;

// Una sentencia del AST. No tiene tipo: su chequeo controla sus expresiones, su
// forma y su contexto (documento §12), y delega en sus hijos (Clase 10, slide 25).
public abstract class NodoSentencia {

    public abstract void chequear(Contexto contexto) throws ExcepcionSemantica;

    // Documento §12.4: la condición de un if o de un while es boolean.
    protected static void chequearCondicion(NodoExpresion condicion, Contexto contexto)
            throws ExcepcionSemantica {
        Tipo tipo = condicion.chequear(contexto);
        if (tipo != TipoPrimitivo.BOOLEAN) {
            throw new ExcepcionSemantica(condicion.token(), "La condicion es de tipo " + tipo.nombre()
                    + " y tiene que ser boolean");
        }
    }

    // Documento §12.3: una declaración local no puede ser el cuerpo directo de un
    // if, un else o un while; hace falta un bloque.
    protected static void chequearCuerpo(NodoSentencia cuerpo, Contexto contexto) throws ExcepcionSemantica {
        if (cuerpo instanceof NodoVarLocal declaracion) {
            throw new ExcepcionSemantica(declaracion.nombre(),
                    "Una declaracion de variable local necesita un bloque para ser el cuerpo de un if, un else o un while");
        }
        cuerpo.chequear(contexto);
    }
}
