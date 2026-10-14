package minijava.semantico.ast.sentencias;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;

// Una sentencia que el sintáctico acepta por un logro de la Etapa 2 cuyo control
// no se persigue: el for y la declaración local con tipo (`int x = 1;`). Los
// programas de la Etapa 4 no la traen; si aparece, es un error.
public final class SentenciaNoControlada extends NodoSentencia {

    private final Token token;

    public SentenciaNoControlada(Token token) {
        this.token = token;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        throw new ExcepcionSemantica(token, "La sentencia que empieza con " + token.lexema()
                + " es de un logro que el compilador no controla");
    }

    @Override
    public String toString() {
        return "(no-controlada " + token.lexema() + ")";
    }
}
