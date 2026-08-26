package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal (se cierra a sí mismo con la comilla doble). '\' escapa el
// carácter siguiente (permite \" embebida); un salto de línea sin cerrar
// antes es error (Aspectos Léxicos §2.3.3).
public final class EstadoStringAbierto implements EstadoLexico {

    public static final EstadoStringAbierto INSTANCIA = new EstadoStringAbierto();

    private EstadoStringAbierto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (contexto.esFinDeArchivo() || actual == '\n') {
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico.STRING_SIN_CERRAR);
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
