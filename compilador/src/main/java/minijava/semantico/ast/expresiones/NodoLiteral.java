package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoNull;
import minijava.semantico.tipos.TipoPrimitivo;

// Documento §11.1: los literales son correctos por sí mismos.
public final class NodoLiteral extends NodoExpresion {

    private final Token literal;

    public NodoLiteral(Token literal) {
        this.literal = literal;
    }

    @Override
    public Tipo chequear(Contexto contexto) {
        return switch (literal.tipo()) {
            case INT_LITERAL -> TipoPrimitivo.INT;
            case CHAR_LITERAL -> TipoPrimitivo.CHAR;
            case FLOAT_LITERAL -> TipoPrimitivo.FLOAT;
            case LIT_STRING -> TablaDeSimbolos.referenciaAString(literal.linea());
            case PR_NULL -> TipoNull.NULL;
            case PR_TRUE, PR_FALSE -> TipoPrimitivo.BOOLEAN;
            default -> throw new IllegalStateException("El parser armo un literal con " + literal);
        };
    }

    @Override
    public Token token() {
        return literal;
    }

    @Override
    public String toString() {
        return literal.lexema();
    }
}
