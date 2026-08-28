package minijava.lexico.estados.comentarios;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.estados.EstadoInicial;

// Descarta hasta el '\n' sin consumirlo —lo trata EstadoInicial como blanco— o hasta EOF.
public final class EstadoComentarioDeLinea implements EstadoLexico {

    public static final EstadoComentarioDeLinea INSTANCIA = new EstadoComentarioDeLinea();

    private EstadoComentarioDeLinea() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            return new Continuar(EstadoInicial.INSTANCIA);
        }
        contexto.descartar();
        return new Continuar(INSTANCIA);
    }
}
