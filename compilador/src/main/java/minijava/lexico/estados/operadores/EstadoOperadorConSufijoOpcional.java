package minijava.lexico.estados.operadores;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

// Los seis pares con sufijo opcional: >/>=, </<=, !/!=, =/==, +/++, -/--.
public final class EstadoOperadorConSufijoOpcional implements EstadoLexico {

    public static final EstadoOperadorConSufijoOpcional MAYOR =
            new EstadoOperadorConSufijoOpcional('=', TipoDeToken.OP_MAYOR_IGUAL, TipoDeToken.OP_MAYOR);
    public static final EstadoOperadorConSufijoOpcional MENOR =
            new EstadoOperadorConSufijoOpcional('=', TipoDeToken.OP_MENOR_IGUAL, TipoDeToken.OP_MENOR);
    public static final EstadoOperadorConSufijoOpcional NEGACION =
            new EstadoOperadorConSufijoOpcional('=', TipoDeToken.OP_DISTINTO, TipoDeToken.OP_NEGACION);
    public static final EstadoOperadorConSufijoOpcional ASIGNACION =
            new EstadoOperadorConSufijoOpcional('=', TipoDeToken.OP_IGUAL, TipoDeToken.OP_ASIGNACION);
    public static final EstadoOperadorConSufijoOpcional SUMA =
            new EstadoOperadorConSufijoOpcional('+', TipoDeToken.OP_INCREMENTO, TipoDeToken.OP_SUMA);
    public static final EstadoOperadorConSufijoOpcional RESTA =
            new EstadoOperadorConSufijoOpcional('-', TipoDeToken.OP_DECREMENTO, TipoDeToken.OP_RESTA);

    private final char sufijo;
    private final TipoDeToken conSufijo;
    private final TipoDeToken sinSufijo;

    private EstadoOperadorConSufijoOpcional(char sufijo, TipoDeToken conSufijo, TipoDeToken sinSufijo) {
        this.sufijo = sufijo;
        this.conSufijo = conSufijo;
        this.sinSufijo = sinSufijo;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        int linea = contexto.getLinea();
        if (contexto.getCaracterActual() == sufijo) {
            contexto.avanzar();
            return new Emitir(new Token(conSufijo, contexto.getLexema(), linea));
        }
        return new Emitir(new Token(sinSufijo, contexto.getLexema(), linea));
    }
}
