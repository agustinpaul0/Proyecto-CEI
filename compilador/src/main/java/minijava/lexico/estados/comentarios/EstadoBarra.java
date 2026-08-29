package minijava.lexico.estados.comentarios;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;

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
            var posicionDeApertura = contexto.posicionPrimerConsumido();
            contexto.iniciarNuevoToken();
            contexto.descartar();
            return new Continuar(new EstadoComentarioMultilinea(posicionDeApertura, false));
        }
        return Emitir.porLexemaFijo(contexto.getLexema(), linea);
    }
}
