package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Tras la barra dentro de un charLiteral cualquier carácter vale: permisividad
// explícita de Aspectos Léxicos §2.3.2, más laxa que Java.
public final class EstadoCharEscapado implements EstadoLexico {

    public static final EstadoCharEscapado INSTANCIA = new EstadoCharEscapado();

    private EstadoCharEscapado() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        // Consumir el salto metería un '\n' crudo en el lexema.
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.CHAR_SIN_CERRAR);
        }
        if (contexto.getCaracterActual() == 'u' && contexto.getConfiguracion().unicodeHabilitado()) {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoCharCerrando.INSTANCIA, false));
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
