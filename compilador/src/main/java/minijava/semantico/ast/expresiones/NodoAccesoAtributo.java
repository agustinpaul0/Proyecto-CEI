package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Atributo;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoReferencia;

// `receptor.atributo`. Documento §10.1: el atributo se busca en el tipo estático
// del receptor, con la sustitución genérica que corresponda. Un arreglo tiene
// sólo `length`, de sólo lectura (§11.8).
public final class NodoAccesoAtributo extends NodoEncadenado {

    private static final String LENGTH = "length";

    // Lo único que no se sabe sin el tipo del receptor: si es el `length` de un
    // arreglo. Lo fija `chequear`, que la asignación corre antes de preguntar.
    private boolean esLengthDeArreglo;

    public NodoAccesoAtributo(NodoExpresion receptor, Token punto, Token nombre) {
        super(receptor, punto, nombre);
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipoDelReceptor = receptor.chequear(contexto);
        switch (tipoDelReceptor) {
            case TipoArreglo arreglo when nombre.lexema().equals(LENGTH) -> {
                esLengthDeArreglo = true;
                return TipoPrimitivo.INT;
            }
            case TipoArreglo arreglo -> throw noDeclarado("El atributo " + nombre.lexema(), tipoDelReceptor);
            case TipoReferencia referencia -> {
                return atributoDe(referencia);
            }
            default -> throw sinMiembros(tipoDelReceptor);
        }
    }

    // Las interfaces no declaran atributos (documento §10.1).
    private Tipo atributoDe(TipoReferencia referencia) throws ExcepcionSemantica {
        ClaseOInterfaz declarado = TablaDeSimbolos.tipo(referencia.nombreDelTipo()).orElseThrow();
        if (!(declarado instanceof Clase clase)) {
            throw noDeclarado("El atributo " + nombre.lexema(), referencia);
        }
        Atributo atributo = clase.atributo(nombre.lexema()).orElseThrow(() ->
                noDeclarado("El atributo " + nombre.lexema(), referencia));
        return atributo.tipo().instanciar(ClaseOInterfaz.sustitucionHacia(referencia, clase));
    }

    @Override
    public boolean esAsignable() {
        return !esLengthDeArreglo && !empiezaConParentesis();
    }

    @Override
    public String toString() {
        return "(atributo " + receptor + " " + nombre.lexema() + ")";
    }
}
