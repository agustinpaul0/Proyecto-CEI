package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la barra invertida dentro de un charLiteral: cualquier carácter es
// válido como escapado, salvo EOF (no hay nada que escapar). 'u' dispara el
// escape Unicode de 4 hex dígitos (logro Unicodes).
public final class EstadoCharEscapado implements EstadoLexico {

    public static final EstadoCharEscapado INSTANCIA = new EstadoCharEscapado();

    private EstadoCharEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.esFinDeArchivo()) {
            throw contexto.crearExcepcionLexica();
        }
        if (contexto.getCaracterActual() == 'u') {
            contexto.avanzar();
            return new Continuar(new EstadoEscapeUnicode(4, EstadoCharCerrando.INSTANCIA));
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
