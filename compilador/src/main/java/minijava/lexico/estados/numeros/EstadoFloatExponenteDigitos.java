package minijava.lexico.estados.numeros;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

public final class EstadoFloatExponenteDigitos implements EstadoLexico {

    public static final EstadoFloatExponenteDigitos INSTANCIA = new EstadoFloatExponenteDigitos();

    private EstadoFloatExponenteDigitos() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        char actual = contexto.getCaracterActual();
        if (Character.isDigit(actual)) {
            contexto.avanzar();
            return new Continuar(INSTANCIA);
        }
        if (EstadosFloat.esSufijoFloat(actual)) {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return new Emitir(new Token(TipoDeToken.FLOAT_LITERAL, contexto.getLexema(), linea));
        }
        return new Emitir(new Token(TipoDeToken.FLOAT_LITERAL, contexto.getLexema(), contexto.getLinea()));
    }
}
