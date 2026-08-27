package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la comilla de apertura. '\x' con x arbitrario es válido (§2.3.2); '' y sin cerrar, error.
public final class EstadoCharAbierto implements EstadoLexico {

    public static final EstadoCharAbierto INSTANCIA = new EstadoCharAbierto();

    private EstadoCharAbierto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (contexto.esFinDeArchivo() || actual == '\n') {
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.CHAR_SIN_CERRAR);
        }
        if (actual == '\'') {
            contexto.avanzar();
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.CHAR_VACIO);
        }
        if (actual == '\\') {
            contexto.avanzar();
            return new Continuar(EstadoCharEscapado.INSTANCIA);
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
