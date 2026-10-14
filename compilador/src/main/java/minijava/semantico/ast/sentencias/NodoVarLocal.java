package minijava.semantico.ast.sentencias;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoNull;
import minijava.semantico.tipos.TipoVoid;

// `var nombre = inicial;`. Documento §12.3: el inicializador se chequea antes de
// declarar la variable, así que no puede nombrarse a sí misma; la variable toma
// exactamente su tipo, que no puede ser el de null ni void. Los errores van en
// el nombre, como en javac.
public final class NodoVarLocal extends NodoSentencia {

    private final Token nombre;
    private final NodoExpresion inicial;

    public NodoVarLocal(Token nombre, NodoExpresion inicial) {
        this.nombre = nombre;
        this.inicial = inicial;
    }

    public Token nombre() {
        return nombre;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipo = inicial.chequear(contexto);
        if (tipo == TipoNull.NULL || tipo == TipoVoid.VOID) {
            throw new ExcepcionSemantica(nombre, "No se puede inferir el tipo de " + nombre.lexema()
                    + " a partir de un inicializador de tipo " + tipo.nombre());
        }
        contexto.declararLocal(nombre, tipo);
    }

    @Override
    public String toString() {
        return "(var " + nombre.lexema() + " " + inicial + ")";
    }
}
