package minijava.semantico.ast.expresiones;

import java.util.List;
import minijava.generacion.ValorDeLiteral;
import minijava.generacion.RutinasDeSoporte;
import minijava.generacion.Generador;
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
    public void generar(Generador g) {
        switch (literal.tipo()) {
            case INT_LITERAL -> g.gen("PUSH " + Integer.parseInt(literal.lexema()));
            case CHAR_LITERAL -> g.gen("PUSH " + ValorDeLiteral.caracter(literal.lexema()));
            // float es de un logro que no se persigue: se apilan sus bits para que
            // el compilador no se caiga, sin operaciones que los usen.
            case FLOAT_LITERAL -> g.gen("PUSH " + Float.floatToIntBits(Float.parseFloat(literal.lexema())));
            case LIT_STRING -> generarString(g);
            case PR_TRUE -> g.gen("PUSH 1");
            default -> g.gen("PUSH 0");
        }
    }

    // Apunte §13: el literal se aloja en el heap cada vez que se evalúa (consulta 5
    // al profesor, pendiente).
    private void generarString(Generador g) {
        List<Integer> caracteres = ValorDeLiteral.cadena(literal.lexema());
        RutinasDeSoporte.alojarString(g, caracteres.size());
        for (int i = 0; i < caracteres.size(); i++) {
            g.gen("DUP");
            g.gen("PUSH " + caracteres.get(i));
            g.gen("STOREREF " + (i + 2));
        }
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
