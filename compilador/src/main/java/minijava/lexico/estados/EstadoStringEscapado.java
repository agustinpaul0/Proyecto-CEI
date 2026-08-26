package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
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
        // Un salto de línea real tras la barra invertida no es un escape:
        // el literal quedó sin cerrar. Se lo trata aparte para no meter el
        // '\n' crudo en el lexema (documentacion/manejo-de-errores.md §6).
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.STRING_SIN_CERRAR);
        }
        char actual = contexto.getCaracterActual();
        // Con el logro Unicodes desactivado la 'u' deja de ser el marcador
        // del escape especial y cae al chequeo de abajo, donde no figura
        // entre los escapes válidos de Java: pasa a ser un escape inválido.
        if (actual == 'u' && contexto.getConfiguracion().unicodeHabilitado()) {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoStringAbierto.INSTANCIA));
        }
        if (ESCAPES_DE_UN_CARACTER.indexOf(actual) < 0 && !esDigitoOctal(actual)) {
            // Error NO fatal, como javac: "illegal escape character" se
            // reporta pero el literal se sigue leyendo hasta su comilla de
            // cierre, así que el litString se emite igual y no hay cascada.
            // (Verificado ejecutando javac sobre "hola \q mundo".) La columna
            // es la del carácter escapado, no la de la barra: también es lo
            // que hace javac. El lexema reportado es el armado hasta acá,
            // como exige Pautas, y la secuencia concreta viaja aparte para
            // que el encabezado pueda nombrarla.
            contexto.avanzar();
            contexto.registrarError(contexto.crearExcepcionLexicaSobreElUltimoConsumido(
                    TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, "\\" + actual));
            return new Continuar(EstadoStringAbierto.INSTANCIA);
        }
        contexto.avanzar();
        return new Continuar(EstadoStringAbierto.INSTANCIA);
    }

    private static boolean esDigitoOctal(char c) {
        return c >= '0' && c <= '7';
    }
}
