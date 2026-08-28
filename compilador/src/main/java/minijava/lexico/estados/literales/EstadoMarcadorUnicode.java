package minijava.lexico.estados.literales;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;

// JLS §3.3: el marcador admite repetir 'u'. Se consumen todas y recién ahí se
// exigen los 4 hex dígitos.
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
