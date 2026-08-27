package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras 'e'/'E': signo opcional, pero al menos un dígito obligatorio. "1e" es
// error, sin backtracking a un token más corto.
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
        // El '^' va donde empieza el literal, no en el lookahead, como javac.
        throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS);
    }
}
