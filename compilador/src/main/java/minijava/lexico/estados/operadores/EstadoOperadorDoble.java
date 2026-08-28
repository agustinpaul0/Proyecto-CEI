package minijava.lexico.estados.operadores;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

// && y ||: el segundo carácter es obligatorio, & y | solos son error (§2.4.2).
public final class EstadoOperadorDoble implements EstadoLexico {

    public static final EstadoOperadorDoble AND = new EstadoOperadorDoble('&', TipoDeToken.OP_AND);
    public static final EstadoOperadorDoble OR = new EstadoOperadorDoble('|', TipoDeToken.OP_OR);

    private final char segundoCaracter;
    private final TipoDeToken tipo;

    private EstadoOperadorDoble(char segundoCaracter, TipoDeToken tipo) {
        this.segundoCaracter = segundoCaracter;
        this.tipo = tipo;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        int linea = contexto.getLinea();
        if (contexto.getCaracterActual() == segundoCaracter) {
            contexto.avanzar();
            return new Emitir(new Token(tipo, contexto.getLexema(), linea));
        }
        throw contexto.crearExcepcionLexica(TipoDeErrorLexico.OPERADOR_INCOMPLETO,
                AnclaDeError.INICIO_DEL_LEXEMA);
    }
}
