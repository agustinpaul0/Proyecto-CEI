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
        // El mensaje habla del literal entero ('1e-'), no del carácter que se
        // está mirando —que es un token perfectamente legítimo y no se
        // consume—, así que el '^' va donde el literal empieza. Es lo que
        // hace javac: "malformed floating-point literal" señala el primer
        // carácter del número (ver agents/manejo-de-errores.md).
        throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS);
    }
}
