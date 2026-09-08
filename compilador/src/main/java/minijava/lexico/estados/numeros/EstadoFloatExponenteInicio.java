package minijava.lexico.estados.numeros;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;

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
        throw contexto.crearExcepcionLexica(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS, AnclaDeError.INICIO_DEL_LEXEMA);
    }
}
