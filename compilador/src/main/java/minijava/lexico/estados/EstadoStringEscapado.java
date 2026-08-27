package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la barra invertida dentro de un litString. A diferencia de charLiteral
// —donde la cátedra permite "\x con x cualquiera" (Aspectos Léxicos §2.3.2)—
// acá sólo valen los escapes del JLS §3.10.7: b,s,t,n,f,r,",',\, un dígito
// octal 0-7, o 'u' (unicode, manejado aparte).
public final class EstadoStringEscapado implements EstadoLexico {

    public static final EstadoStringEscapado INSTANCIA = new EstadoStringEscapado();

    private static final String ESCAPES_DE_UN_CARACTER = "bstnfr\"'\\";

    private EstadoStringEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        // Un salto de línea real tras la barra invertida no es un escape: el
        // literal quedó sin cerrar. Se lo trata aparte para no meter el '\n'
        // crudo en el lexema (documentacion/manejo-de-errores.md §6).
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.STRING_SIN_CERRAR);
        }
        char actual = contexto.getCaracterActual();
        // Con el logro Unicodes desactivado la 'u' deja de ser el marcador del
        // escape especial y cae al chequeo de abajo, donde no figura entre los
        // escapes válidos de Java: pasa a ser un escape inválido.
        if (actual == 'u' && contexto.getConfiguracion().unicodeHabilitado()) {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoStringAbierto.INSTANCIA, true));
        }
        if (ESCAPES_DE_UN_CARACTER.indexOf(actual) < 0 && !esDigitoOctal(actual)) {
            // La excepción se arma ANTES de resincronizar: fotografía el
            // lexema armado hasta acá y la columna del carácter escapado (no
            // la de la barra, igual que javac). El descarte posterior no debe
            // aparecer en ninguno de los dos datos.
            contexto.avanzar();
            ExcepcionLexica error = contexto.crearExcepcionLexicaSobreElUltimoConsumido(
                    TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, "\\" + actual);
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
