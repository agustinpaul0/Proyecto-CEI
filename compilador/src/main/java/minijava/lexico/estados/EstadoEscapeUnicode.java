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

    public EstadoEscapeUnicode(int digitosRestantes, EstadoLexico siguienteAlTerminar) {
        this.digitosRestantes = digitosRestantes;
        this.siguienteAlTerminar = siguienteAlTerminar;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (digitosRestantes == 0) {
            return new Continuar(siguienteAlTerminar);
        }
        if (!esHexadecimal(contexto.getCaracterActual())) {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.UNICODE_MAL_FORMADO);
        }
        contexto.avanzar();
        return new Continuar(new EstadoEscapeUnicode(digitosRestantes - 1, siguienteAlTerminar));
    }

    private static boolean esHexadecimal(char c) {
        return Character.digit(c, 16) != -1;
    }
}
