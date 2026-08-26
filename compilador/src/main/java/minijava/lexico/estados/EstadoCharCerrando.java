package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

public final class EstadoCharCerrando implements EstadoLexico {

    public static final EstadoCharCerrando INSTANCIA = new EstadoCharCerrando();

    private EstadoCharCerrando() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.getCaracterActual() != '\'') {
            // El literal "nunca se cerró" sólo si lo interrumpió el fin de
            // línea o el fin de archivo; si lo interrumpió un carácter
            // cualquiera, el literal está mal formado (tiene más de uno).
            boolean seInterrumpio = contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n';
            throw contexto.crearExcepcionLexica(seInterrumpio
                    ? TipoDeErrorLexico.CHAR_SIN_CERRAR
                    : TipoDeErrorLexico.CHAR_MAL_FORMADO);
        }
        int linea = contexto.getLinea();
        contexto.avanzar();
        return new Emitir(new Token(TipoDeToken.CHAR_LITERAL, contexto.getLexema(), linea));
    }
}
