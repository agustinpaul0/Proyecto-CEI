package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras 'e'/'E': el signo es opcional pero al menos un dígito es obligatorio
// después: "1e"/"1.5e" son error, sin backtracking a un token más corto
// (agents/especificacion-lexica.md).
public final class EstadoFloatExponenteInicio implements EstadoLexico {

    public static final EstadoFloatExponenteInicio INSTANCIA = new EstadoFloatExponenteInicio();

    private EstadoFloatExponenteInicio() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (actual == '+' || actual == '-') {
            contexto.avanzar();
            return new Continuar(EstadoFloatExponenteRequiereDigito.INSTANCIA);
        }
        if (Character.isDigit(actual)) {
            contexto.avanzar();
            return new Continuar(EstadoFloatExponenteDigitos.INSTANCIA);
        }
        // El mensaje habla del literal entero ('1e'), no del carácter que se
        // está mirando —que es un token perfectamente legítimo y no se
        // consume—, así que el '^' va donde el literal empieza. Es lo que
        // hace javac: "malformed floating-point literal" señala el primer
        // carácter del número (ver agents/manejo-de-errores.md).
        throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS);
    }
}
