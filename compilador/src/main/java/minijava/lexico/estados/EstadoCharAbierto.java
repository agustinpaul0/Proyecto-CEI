package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la comilla simple de apertura. '\x' con x arbitrario es válido
// (Aspectos Léxicos §2.3.2); '' vacío y sin cerrar son error.
public final class EstadoCharAbierto implements EstadoLexico {

    public static final EstadoCharAbierto INSTANCIA = new EstadoCharAbierto();

    private EstadoCharAbierto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (contexto.esFinDeArchivo() || actual == '\n') {
            throw contexto.crearExcepcionLexica();
        }
        if (actual == '\'') {
            contexto.agregarCaracterOfensivo();
            throw contexto.crearExcepcionLexica();
        }
        if (actual == '\\') {
            contexto.avanzar();
            return new Continuar(EstadoCharEscapado.INSTANCIA);
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
