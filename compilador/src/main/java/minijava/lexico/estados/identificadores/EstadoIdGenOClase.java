package minijava.lexico.estados.identificadores;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

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
