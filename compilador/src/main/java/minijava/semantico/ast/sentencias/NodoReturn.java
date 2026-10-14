package minijava.semantico.ast.sentencias;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoVoid;

// Documento §12.5. Sin expresión, el error va en el `return`; con expresión, en
// la expresión, como en javac.
public final class NodoReturn extends NodoSentencia {

    private final Token palabraReturn;
    private final NodoExpresion expresion;

    public NodoReturn(Token palabraReturn, NodoExpresion expresion) {
        this.palabraReturn = palabraReturn;
        this.expresion = expresion;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        if (expresion == null) {
            chequearSinValor(contexto);
        } else {
            chequearConValor(contexto);
        }
    }

    private void chequearSinValor(Contexto contexto) throws ExcepcionSemantica {
        if (!contexto.esConstructor() && contexto.retorno() != TipoVoid.VOID) {
            throw new ExcepcionSemantica(palabraReturn, "Falta el valor de retorno, de tipo "
                    + contexto.retorno().nombre());
        }
    }

    private void chequearConValor(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipo = expresion.chequear(contexto);
        if (contexto.esConstructor()) {
            throw new ExcepcionSemantica(expresion.token(), "Un constructor no retorna un valor");
        }
        Tipo esperado = contexto.retorno();
        if (esperado == TipoVoid.VOID) {
            throw new ExcepcionSemantica(expresion.token(), "Un metodo void no retorna un valor");
        }
        if (!tipo.conformaCon(esperado)) {
            throw new ExcepcionSemantica(expresion.token(), "Se retorna un valor de tipo " + tipo.nombre()
                    + ", que no conforma con " + esperado.nombre());
        }
    }

    @Override
    public String toString() {
        return expresion == null ? "(return)" : "(return " + expresion + ")";
    }
}
