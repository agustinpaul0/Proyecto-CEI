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

// Terminal: intLiteral, salvo que la corrida de dígitos continúe hacia un
// floatLiteral (logro Floats I). Se consume la corrida completa de dígitos
// (maximal munch) y recién ahí se valida el límite de 9 dígitos, para no
// cortar un float largo a mitad de la parte entera: '1234567890.5' es un
// float válido con diez dígitos en la parte entera, mientras que
// '1234567890' solo es error (agents/especificacion-lexica.md).
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
        // Con el logro Floats I desactivado, la corrida de dígitos no
        // continúa hacia un floatLiteral: '.', 'e'/'E' y los sufijos
        // terminan el intLiteral como cualquier otro carácter no numérico.
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
