package minijava.lexico.estados.identificadores;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

// Terminal: idClase, ya con al menos 2 caracteres.
public final class EstadoIdClase implements EstadoLexico {

    public static final EstadoIdClase INSTANCIA = new EstadoIdClase();

    private EstadoIdClase() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        if (EstadoIdMV.esAlfanumericoOGuion(contexto.getCaracterActual())) {
            contexto.avanzar();
            return new Continuar(INSTANCIA);
        }
        return new Emitir(new Token(TipoDeToken.ID_CLASE, contexto.getLexema(), contexto.getLinea()));
    }
}
