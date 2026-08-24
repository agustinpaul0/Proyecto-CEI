package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal: idClase, ya con al menos 2 caracteres (llegado desde EstadoIdGenOClase).
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
