package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Cubre && y ||: el segundo carácter es obligatorio (& y | solos son error,
// no están en la lista de operadores de Aspectos Léxicos §2.4.2).
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
        throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.OPERADOR_INCOMPLETO);
    }
}
