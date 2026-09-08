package minijava.lexico.estados.numeros;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;

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
        throw contexto.crearExcepcionLexica(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS, AnclaDeError.INICIO_DEL_LEXEMA);
    }
}
