package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Descarta caracteres hasta el fin de línea (sin consumir el '\n', para que
// EstadoInicial lo procese como blanco normal) o hasta EOF.
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
