package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la barra invertida dentro de un litString: consume el carácter
// escapado sin analizar el string por él (evita que \" cierre el string).
// 'u' dispara el escape Unicode (logro Unicodes) — ver EstadoMarcadorUnicode.
public final class EstadoStringEscapado implements EstadoLexico {

    public static final EstadoStringEscapado INSTANCIA = new EstadoStringEscapado();

    private EstadoStringEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.esFinDeArchivo()) {
            throw contexto.crearExcepcionLexica();
        }
        if (contexto.getCaracterActual() == 'u') {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoStringAbierto.INSTANCIA));
        }
        contexto.avanzar();
        return new Continuar(EstadoStringAbierto.INSTANCIA);
    }
}
