package minijava.lexico.estados.literales;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;

// Tras la comilla de apertura. '\x' con x arbitrario es válido (§2.3.2); '' y sin cerrar, error.
public final class EstadoCharAbierto implements EstadoLexico {

    public static final EstadoCharAbierto INSTANCIA = new EstadoCharAbierto();

    private EstadoCharAbierto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (contexto.esFinDeArchivo() || actual == '\n') {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.CHAR_SIN_CERRAR,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        if (actual == '\'') {
            contexto.avanzar();
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.CHAR_VACIO,
                    AnclaDeError.ULTIMO_CONSUMIDO);
        }
        if (actual == '\\') {
            contexto.avanzar();
            return new Continuar(EstadoCharEscapado.INSTANCIA);
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
