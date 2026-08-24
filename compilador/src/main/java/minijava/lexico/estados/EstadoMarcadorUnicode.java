package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// JLS §3.3: el marcador de un escape Unicode admite repetir 'u' una o más
// veces (\uu0041, \uuu0041, ...). Se consumen todas las 'u' seguidas y recién
// ahí se exigen los 4 hex dígitos — semántica de Java heredada tal cual, sin
// especificación propia de la cátedra (AGENTS.md §5).
public final class EstadoMarcadorUnicode implements EstadoLexico {

    private final EstadoLexico siguienteAlTerminar;

    public EstadoMarcadorUnicode(EstadoLexico siguienteAlTerminar) {
        this.siguienteAlTerminar = siguienteAlTerminar;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.getCaracterActual() == 'u') {
            contexto.avanzar();
            return new Continuar(this);
        }
        return new Continuar(new EstadoEscapeUnicode(4, siguienteAlTerminar));
    }
}
