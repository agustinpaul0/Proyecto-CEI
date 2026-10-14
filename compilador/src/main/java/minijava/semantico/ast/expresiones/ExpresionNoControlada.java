package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

// Una construcción que el sintáctico acepta por un logro de la Etapa 2 cuyo
// control no se persigue: el ternario, los posfijos, el inicializador de
// arreglo y el diamante. Los programas de la Etapa 4 no la traen; si aparece,
// es un error y no se acepta sin chequear.
public final class ExpresionNoControlada extends NodoExpresion {

    private final Token token;

    public ExpresionNoControlada(Token token) {
        this.token = token;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        throw new ExcepcionSemantica(token, "La construccion con " + token.lexema()
                + " es de un logro que el compilador no controla");
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public String toString() {
        return "(no-controlada " + token.lexema() + ")";
    }
}
