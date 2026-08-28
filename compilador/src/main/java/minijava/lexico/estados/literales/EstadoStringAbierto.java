package minijava.lexico.estados.literales;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

// Terminal, se cierra con la comilla doble. '\' escapa el siguiente (permite \"
// embebida); un salto de línea antes del cierre es error (Aspectos Léxicos §2.3.3).
public final class EstadoStringAbierto implements EstadoLexico {

    public static final EstadoStringAbierto INSTANCIA = new EstadoStringAbierto();

    private EstadoStringAbierto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (contexto.esFinDeArchivo() || actual == '\n') {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.STRING_SIN_CERRAR,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        if (actual == '"') {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return new Emitir(new Token(TipoDeToken.LIT_STRING, contexto.getLexema(), linea));
        }
        if (actual == '\\') {
            contexto.avanzar();
            return new Continuar(EstadoStringEscapado.INSTANCIA);
        }
        contexto.avanzar();
        return new Continuar(INSTANCIA);
    }
}
