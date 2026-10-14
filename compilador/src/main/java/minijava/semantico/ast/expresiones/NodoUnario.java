package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoPrimitivo;

// Documento §11.2: `+` y `-` sobre int, `!` sobre boolean.
public final class NodoUnario extends NodoExpresion {

    private final Token operador;
    private final NodoExpresion operando;

    public NodoUnario(Token operador, NodoExpresion operando) {
        this.operador = operador;
        this.operando = operando;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipo = operando.chequear(contexto);
        Tipo esperado = operador.tipo() == TipoDeToken.OP_NEGACION ? TipoPrimitivo.BOOLEAN : TipoPrimitivo.INT;
        if (tipo != esperado) {
            throw new ExcepcionSemantica(operador, "El operador " + operador.lexema()
                    + " no admite el tipo " + tipo.nombre());
        }
        return esperado;
    }

    @Override
    public Token token() {
        return operador;
    }

    @Override
    public String toString() {
        return "(" + operador.lexema() + " " + operando + ")";
    }
}
