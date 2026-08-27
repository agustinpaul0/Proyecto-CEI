package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal tras una única mayúscula: si sigue un carácter de identificador es
// idClase (mínimo 2), si no es idGen (Clase 01, slides 9-11).
public final class EstadoIdGenOClase implements EstadoLexico {

    public static final EstadoIdGenOClase INSTANCIA = new EstadoIdGenOClase();

    private EstadoIdGenOClase() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        if (EstadoIdMV.esAlfanumericoOGuion(contexto.getCaracterActual())) {
            contexto.avanzar();
            return new Continuar(EstadoIdClase.INSTANCIA);
        }
        return new Emitir(new Token(TipoDeToken.ID_GEN, contexto.getLexema(), contexto.getLinea()));
    }
}
