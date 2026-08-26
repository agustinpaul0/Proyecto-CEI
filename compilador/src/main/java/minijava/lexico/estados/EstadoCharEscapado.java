package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la barra invertida dentro de un charLiteral: cualquier carácter es
// válido como escapado, salvo EOF (no hay nada que escapar). 'u' dispara el
// escape Unicode (logro Unicodes) — ver EstadoMarcadorUnicode.
public final class EstadoCharEscapado implements EstadoLexico {

    public static final EstadoCharEscapado INSTANCIA = new EstadoCharEscapado();

    private EstadoCharEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        // Igual que en litString: un salto de línea real tras la barra
        // invertida deja el char sin cerrar, y consumirlo metería un '\n'
        // crudo en el lexema, partiendo [Error:Lexema|NroLinea] en dos.
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.CHAR_SIN_CERRAR);
        }
        if (contexto.getCaracterActual() == 'u' && contexto.getConfiguracion().unicodeHabilitado()) {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoCharCerrando.INSTANCIA));
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
