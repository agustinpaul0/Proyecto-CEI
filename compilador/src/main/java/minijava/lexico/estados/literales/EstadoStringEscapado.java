package minijava.lexico.estados.literales;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.recuperacion.RecuperacionDeLitString;

// Tras la barra dentro de un litString. A diferencia de charLiteral, acá sólo
// valen los escapes del JLS §3.10.7: b,s,t,n,f,r,",',\, un octal 0-7, o 'u'.
public final class EstadoStringEscapado implements EstadoLexico {

    public static final EstadoStringEscapado INSTANCIA = new EstadoStringEscapado();

    private static final String ESCAPES_DE_UN_CARACTER = "bstnfr\"'\\";

    private EstadoStringEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        // Aparte, para no meter el '\n' crudo en el lexema.
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.STRING_SIN_CERRAR,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        char actual = contexto.getCaracterActual();
        // Sin el logro Unicodes la 'u' cae al chequeo de abajo, donde no figura:
        // pasa a ser un escape inválido.
        if (actual == 'u' && contexto.getConfiguracion().unicodeHabilitado()) {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoStringAbierto.INSTANCIA, true));
        }
        if (ESCAPES_DE_UN_CARACTER.indexOf(actual) < 0 && !esDigitoOctal(actual)) {
            // Se arma ANTES de resincronizar: el descarte posterior no debe entrar
            // ni en el lexema ni en la columna.
            contexto.avanzar();
            ExcepcionLexica error = contexto.crearExcepcionLexica(
                    TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, AnclaDeError.ULTIMO_CONSUMIDO,
                    "\\" + actual);
            RecuperacionDeLitString.descartarHastaElCierre(contexto);
            throw error;
        }
        contexto.avanzar();
        return new Continuar(EstadoStringAbierto.INSTANCIA);
    }

    private static boolean esDigitoOctal(char c) {
        return c >= '0' && c <= '7';
    }
}
