package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoPrimitivo;

// Documento §11.2. El error va en el operador (Pautas Etapa 4, slide 30).
public final class NodoBinario extends NodoExpresion {

    private final NodoExpresion izquierda;
    private final Token operador;
    private final NodoExpresion derecha;

    public NodoBinario(NodoExpresion izquierda, Token operador, NodoExpresion derecha) {
        this.izquierda = izquierda;
        this.operador = operador;
        this.derecha = derecha;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipoIzquierdo = izquierda.chequear(contexto);
        Tipo tipoDerecho = derecha.chequear(contexto);
        return switch (operador.tipo()) {
            case OP_IGUAL, OP_DISTINTO -> igualdad(tipoIzquierdo, tipoDerecho);
            case OP_AND, OP_OR -> operandos(tipoIzquierdo, tipoDerecho, TipoPrimitivo.BOOLEAN, TipoPrimitivo.BOOLEAN);
            case OP_MENOR, OP_MAYOR, OP_MENOR_IGUAL, OP_MAYOR_IGUAL ->
                    operandos(tipoIzquierdo, tipoDerecho, TipoPrimitivo.INT, TipoPrimitivo.BOOLEAN);
            default -> operandos(tipoIzquierdo, tipoDerecho, TipoPrimitivo.INT, TipoPrimitivo.INT);
        };
    }

    private Tipo operandos(Tipo tipoIzquierdo, Tipo tipoDerecho, Tipo operando, Tipo resultado)
            throws ExcepcionSemantica {
        if (tipoIzquierdo != operando || tipoDerecho != operando) {
            throw error(tipoIzquierdo, tipoDerecho);
        }
        return resultado;
    }

    // Conformantes en alguna dirección. void no conforma con nada.
    private Tipo igualdad(Tipo tipoIzquierdo, Tipo tipoDerecho) throws ExcepcionSemantica {
        if (!tipoIzquierdo.conformaCon(tipoDerecho) && !tipoDerecho.conformaCon(tipoIzquierdo)) {
            throw error(tipoIzquierdo, tipoDerecho);
        }
        return TipoPrimitivo.BOOLEAN;
    }

    private ExcepcionSemantica error(Tipo tipoIzquierdo, Tipo tipoDerecho) {
        return new ExcepcionSemantica(operador, "El operador " + operador.lexema() + " no admite los tipos "
                + tipoIzquierdo.nombre() + " y " + tipoDerecho.nombre());
    }

    @Override
    public Token token() {
        return operador;
    }

    @Override
    public String toString() {
        return "(" + operador.lexema() + " " + izquierda + " " + derecha + ")";
    }
}
