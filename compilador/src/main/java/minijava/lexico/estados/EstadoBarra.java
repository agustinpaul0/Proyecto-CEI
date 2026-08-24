package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Tras un primer '/': desambigua entre división, comentario de línea (//) y
// comentario multilínea (/*).
public final class EstadoBarra implements EstadoLexico {

    public static final EstadoBarra INSTANCIA = new EstadoBarra();

    private EstadoBarra() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        int linea = contexto.getLinea();
        char actual = contexto.getCaracterActual();

        if (actual == '/') {
            contexto.iniciarNuevoToken();
            contexto.descartar();
            return new Continuar(EstadoComentarioDeLinea.INSTANCIA);
        }
        if (actual == '*') {
            contexto.iniciarNuevoToken();
            contexto.descartar();
            return new Continuar(new EstadoComentarioMultilinea(linea, false));
        }
        return new Emitir(new Token(TipoDeToken.OP_DIV, contexto.getLexema(), linea));
    }
}
