package minijava.semantico.ast.sentencias;

import minijava.semantico.entidades.Unidad;
import minijava.generacion.Generador;
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
    private Unidad unidad;
    private int localesVivas;

    public NodoReturn(Token palabraReturn, NodoExpresion expresion) {
        this.palabraReturn = palabraReturn;
        this.expresion = expresion;
    }

    @Override
    public void chequear(Contexto contexto) throws ExcepcionSemantica {
        unidad = contexto.unidad();
        localesVivas = contexto.localesVivas();
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

    // Apunte §8.1: el valor va a la celda del resultado, se liberan todas las
    // locales vivas, no sólo las del bloque, y la unidad termina.
    @Override
    public void generar(Generador g) {
        if (expresion != null) {
            expresion.generar(g);
            g.gen("STORE " + unidad.offsetDeRetorno());
        }
        if (localesVivas > 0) {
            g.gen("FMEM " + localesVivas);
        }
        unidad.generarFin(g);
    }

    @Override
    public String toString() {
        return expresion == null ? "(return)" : "(return " + expresion + ")";
    }
}
