package minijava.lexico.estados.numeros;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;

// Terminal: tras "N." la parte fraccionaria es opcional, porque Java acepta "1."
// (verificado con javac).
public final class EstadoFloatConPunto implements EstadoLexico {

    public static final EstadoFloatConPunto INSTANCIA = new EstadoFloatConPunto();

    private EstadoFloatConPunto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        char actual = contexto.getCaracterActual();
        if (Character.isDigit(actual)) {
            contexto.avanzar();
            return new Continuar(INSTANCIA);
        }
        if (actual == 'e' || actual == 'E') {
            contexto.avanzar();
            return new Continuar(EstadoFloatExponenteInicio.INSTANCIA);
        }
        if (EstadosFloat.esSufijoFloat(actual)) {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return new Emitir(new Token(TipoDeToken.FLOAT_LITERAL, contexto.getLexema(), linea));
        }
        return new Emitir(new Token(TipoDeToken.FLOAT_LITERAL, contexto.getLexema(), contexto.getLinea()));
    }
}
