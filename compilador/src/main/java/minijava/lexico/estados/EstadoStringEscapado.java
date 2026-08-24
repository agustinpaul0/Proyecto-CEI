package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la barra invertida dentro de un litString: consume el carácter
// escapado sin analizar el string por él (evita que \" cierre el string).
// 'u' dispara el escape Unicode (logro Unicodes) — ver EstadoMarcadorUnicode.
//
// A diferencia de charLiteral (donde la cátedra dice explícitamente "\x con
// x cualquiera" — Aspectos Lexicos §2.3.2), litString no tiene esa
// permisividad explícita (§2.3.3 sólo cita a Java para el caso \"). Acá
// aplica la regla "ante la duda, como en Java" (AGENTS.md §0.1): sólo son
// válidos los escapes que reconoce el JLS §3.10.7 (verificado contra la
// especificación, no de memoria) — b,s,t,n,f,r,",',\, un dígito octal 0-7,
// o 'u' (unicode, manejado aparte). Cualquier otro carácter es error.
public final class EstadoStringEscapado implements EstadoLexico {

    public static final EstadoStringEscapado INSTANCIA = new EstadoStringEscapado();

    private static final String ESCAPES_DE_UN_CARACTER = "bstnfr\"'\\";

    private EstadoStringEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.esFinDeArchivo()) {
            throw contexto.crearExcepcionLexica();
        }
        char actual = contexto.getCaracterActual();
        if (actual == 'u') {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoStringAbierto.INSTANCIA));
        }
        if (ESCAPES_DE_UN_CARACTER.indexOf(actual) < 0 && !esDigitoOctal(actual)) {
            contexto.agregarCaracterOfensivo();
            throw contexto.crearExcepcionLexica();
        }
        contexto.avanzar();
        return new Continuar(EstadoStringAbierto.INSTANCIA);
    }

    private static boolean esDigitoOctal(char c) {
        return c >= '0' && c <= '7';
    }
}
