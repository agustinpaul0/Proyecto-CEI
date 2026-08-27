package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// JLS §3.3: el marcador de un escape Unicode admite repetir 'u' una o más
// veces. Se consumen todas las 'u' seguidas y recién ahí se exigen los 4 hex
// dígitos.
public final class EstadoMarcadorUnicode implements EstadoLexico {

    private final EstadoLexico siguienteAlTerminar;
    private final boolean dentroDeLitString;

    public EstadoMarcadorUnicode(EstadoLexico siguienteAlTerminar, boolean dentroDeLitString) {
        this.siguienteAlTerminar = siguienteAlTerminar;
        this.dentroDeLitString = dentroDeLitString;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.getCaracterActual() == 'u') {
            contexto.avanzar();
            return new Continuar(this);
        }
        return new Continuar(new EstadoEscapeUnicode(4, siguienteAlTerminar, dentroDeLitString));
    }
}
