package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras 'e'/'E': el signo es opcional pero al menos un dígito es obligatorio
// después (AGENTS.md §5: "1e"/"1.5e" sin dígitos → error, sin backtracking).
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
        // Familia B: lo invalido es el lexema ya construido ('1e'), no
        // el caracter que se esta mirando, que es un token perfectamente
        // legitimo y no se consume. Reportar su columna era enganoso y
        // ademas inconsistente: con un salto de linea detras, el fallback
        // de crearExcepcionLexica() ya elegia el ultimo consumido.
        throw contexto.crearExcepcionLexicaSobreElUltimoConsumido(
                TipoDeErrorLexico.EXPONENTE_SIN_DIGITOS);
    }
}
