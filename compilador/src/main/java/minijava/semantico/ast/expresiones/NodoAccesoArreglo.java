package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoPrimitivo;

// `receptor[indice]`. Documento §11.8: el receptor es un arreglo, el índice es
// int y el resultado tiene una dimensión menos. Es asignable.
public final class NodoAccesoArreglo extends NodoExpresion {

    private final NodoExpresion receptor;
    private final Token corchete;
    private final NodoExpresion indice;

    public NodoAccesoArreglo(NodoExpresion receptor, Token corchete, NodoExpresion indice) {
        this.receptor = receptor;
        this.corchete = corchete;
        this.indice = indice;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipoDelReceptor = receptor.chequear(contexto);
        Tipo tipoDelIndice = indice.chequear(contexto);
        if (!(tipoDelReceptor instanceof TipoArreglo arreglo)) {
            throw new ExcepcionSemantica(corchete, "Se indexa una expresion de tipo "
                    + tipoDelReceptor.nombre() + ", que no es un arreglo");
        }
        if (tipoDelIndice != TipoPrimitivo.INT) {
            throw new ExcepcionSemantica(indice.token(), "El indice es de tipo "
                    + tipoDelIndice.nombre() + " y tiene que ser int");
        }
        return arreglo.componente();
    }

    @Override
    public Token token() {
        return corchete;
    }

    @Override
    public boolean esAsignable() {
        return !empiezaConParentesis();
    }

    @Override
    public boolean empiezaConParentesis() {
        return receptor.empiezaConParentesis();
    }

    @Override
    public String toString() {
        return "([] " + receptor + " " + indice + ")";
    }
}
