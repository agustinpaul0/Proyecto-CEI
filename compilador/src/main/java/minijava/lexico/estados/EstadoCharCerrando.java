package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal: espera la comilla simple de cierre.
public final class EstadoCharCerrando implements EstadoLexico {

    public static final EstadoCharCerrando INSTANCIA = new EstadoCharCerrando();

    private EstadoCharCerrando() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.getCaracterActual() != '\'') {
            throw contexto.crearExcepcionLexica();
        }
        int linea = contexto.getLinea();
        contexto.avanzar();
        return new Emitir(new Token(TipoDeToken.CHAR_LITERAL, contexto.getLexema(), linea));
    }
}
