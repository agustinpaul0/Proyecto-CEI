package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal: tras "N.", la parte fraccionaria es opcional (Clase 01 §Floats:
// "misma estructura que los floats de Java" — Java permite "1." sin dígitos
// después del punto).
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
