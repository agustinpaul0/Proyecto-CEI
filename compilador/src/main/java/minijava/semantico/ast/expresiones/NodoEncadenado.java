package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

// `receptor.nombre`, con o sin argumentos. Cada encadenado guarda su receptor:
// `a.b.c` es el acceso a `c` sobre el acceso a `b` sobre `a`, así que el último
// acceso es el de afuera y decide si la expresión es asignable o una llamada
// (documento §11.5). Los errores de selección van en el punto, como en javac; los
// de los argumentos, en el nombre del método, como en la cátedra.
public abstract class NodoEncadenado extends NodoExpresion {

    protected final NodoExpresion receptor;
    protected final Token punto;
    protected final Token nombre;

    protected NodoEncadenado(NodoExpresion receptor, Token punto, Token nombre) {
        this.receptor = receptor;
        this.punto = punto;
        this.nombre = nombre;
    }

    @Override
    public Token token() {
        return punto;
    }

    @Override
    public boolean empiezaConParentesis() {
        return receptor.empiezaConParentesis();
    }

    // Documento §10.1: primitivos, void, null y parámetros de tipo no tienen
    // miembros que seleccionar por punto.
    protected ExcepcionSemantica sinMiembros(Tipo tipoDelReceptor) {
        return new ExcepcionSemantica(punto, "El tipo " + tipoDelReceptor.nombre()
                + " no tiene miembros que seleccionar con punto");
    }

    protected ExcepcionSemantica noDeclarado(String miembro, Tipo tipoDelReceptor) {
        return new ExcepcionSemantica(punto, miembro + " no esta declarado en " + tipoDelReceptor.nombre());
    }
}
