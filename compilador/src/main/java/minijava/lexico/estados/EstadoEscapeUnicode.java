package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

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
            ExcepcionLexica error = contexto.crearExcepcionLexica(TipoDeErrorLexico.UNICODE_MAL_FORMADO);
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
