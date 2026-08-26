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
        // Familia B: lo invalido es el lexema ya construido ('1e'), no
        // el caracter que se esta mirando, que es un token perfectamente
        // legitimo y no se consume. Reportar su columna era enganoso y
        // ademas inconsistente: con un salto de linea detras, el fallback
        // de crearExcepcionLexica() ya elegia el ultimo consumido.
        throw contexto.crearExcepcionLexicaSobreElUltimoConsumido(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS);
    }
}
