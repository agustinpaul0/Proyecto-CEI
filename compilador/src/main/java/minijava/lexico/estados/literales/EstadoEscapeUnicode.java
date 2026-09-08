package minijava.lexico.estados.literales;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.recuperacion.RecuperacionDeLitString;

public final class EstadoEscapeUnicode implements EstadoLexico {

    private final int digitosRestantes;
    private final EstadoLexico siguienteAlTerminar;
    // En un litString el error resincroniza en la comilla de cierre; en un
    // charLiteral no, la recuperación es estructural.
    private final boolean dentroDeLitString;

    public EstadoEscapeUnicode(int digitosRestantes, EstadoLexico siguienteAlTerminar,
                               boolean dentroDeLitString) {
        this.digitosRestantes = digitosRestantes;
        this.siguienteAlTerminar = siguienteAlTerminar;
        this.dentroDeLitString = dentroDeLitString;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (digitosRestantes == 0) {
            return new Continuar(siguienteAlTerminar);
        }
        if (!esHexadecimal(contexto.getCaracterActual())) {
            ExcepcionLexica error = contexto.crearExcepcionLexica(TipoDeErrorLexico.UNICODE_MAL_FORMADO,
                    AnclaDeError.LOOKAHEAD);
            if (dentroDeLitString) {
                RecuperacionDeLitString.descartarHastaElCierre(contexto);
            }
            throw error;
        }
        contexto.avanzar();
        return new Continuar(new EstadoEscapeUnicode(digitosRestantes - 1, siguienteAlTerminar,
                dentroDeLitString));
    }

    private static boolean esHexadecimal(char c) {
        return Character.digit(c, 16) != -1;
    }
}
