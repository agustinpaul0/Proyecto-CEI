package minijava.lexico.estados.literales;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;

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
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.CHAR_SIN_CERRAR,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        if (contexto.getCaracterActual() == 'u') {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoCharCerrando.INSTANCIA, false));
        }
        contexto.avanzar();
        return new Continuar(EstadoCharCerrando.INSTANCIA);
    }
}
