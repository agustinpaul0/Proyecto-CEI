package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

public final class EstadoFloatExponenteRequiereDigito implements EstadoLexico {

    public static final EstadoFloatExponenteRequiereDigito INSTANCIA =
            new EstadoFloatExponenteRequiereDigito();

    private EstadoFloatExponenteRequiereDigito() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (Character.isDigit(contexto.getCaracterActual())) {
            contexto.avanzar();
            return new Continuar(EstadoFloatExponenteDigitos.INSTANCIA);
        }
        throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS);
    }
}
