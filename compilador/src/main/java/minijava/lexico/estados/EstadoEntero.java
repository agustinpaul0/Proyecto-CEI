package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal: intLiteral, salvo que la corrida siga hacia un floatLiteral. El
// límite de 9 dígitos se valida tras consumirla entera, por '1234567890.5'.
public final class EstadoEntero implements EstadoLexico {

    public static final EstadoEntero INSTANCIA = new EstadoEntero();

    private static final int MAXIMO_DIGITOS = 9;

    private EstadoEntero() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (Character.isDigit(actual)) {
            contexto.avanzar();
            return new Continuar(INSTANCIA);
        }
        // Sin el logro Floats I, '.', 'e'/'E' y los sufijos cierran el intLiteral.
        if (contexto.getConfiguracion().floatsHabilitados()) {
            if (actual == '.') {
                contexto.avanzar();
                return new Continuar(EstadoFloatConPunto.INSTANCIA);
            }
            if (actual == 'e' || actual == 'E') {
                contexto.avanzar();
                return new Continuar(EstadoFloatExponenteInicio.INSTANCIA);
            }
            if (EstadosFloat.esSufijoFloat(actual)) {
                int linea = contexto.getLinea();
                contexto.avanzar();
                return new Emitir(new Token(TipoDeToken.FLOAT_LITERAL, contexto.getLexema(), linea));
            }
        }

        String lexema = contexto.getLexema();
        if (lexema.length() > MAXIMO_DIGITOS) {
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.ENTERO_DEMASIADO_LARGO);
        }
        return new Emitir(new Token(TipoDeToken.INT_LITERAL, lexema, contexto.getLinea()));
    }
}
