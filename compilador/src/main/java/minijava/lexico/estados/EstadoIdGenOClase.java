package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal: tras una única letra mayúscula. Si sigue un carácter de
// identificador es idClase (mínimo 2 caracteres); si no, es idGen (Clase 01
// slide 9-11 — mismo patrón de no-determinismo/fusión que Mayor/MayorIgual,
// aplicado a los identificadores reales de MiniJava).
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
