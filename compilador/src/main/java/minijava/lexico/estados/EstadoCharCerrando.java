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
            // "Sin cerrar" sólo si lo cortó el fin de línea o de archivo; otro
            // carácter lo deja "mal formado".
            boolean seInterrumpio = contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n';
            throw contexto.crearExcepcionLexicaSobreElInicioDelLexema(seInterrumpio
                    ? TipoDeErrorLexico.CHAR_SIN_CERRAR
                    : TipoDeErrorLexico.CHAR_MAL_FORMADO);
        }
        int linea = contexto.getLinea();
        contexto.avanzar();
        return new Emitir(new Token(TipoDeToken.CHAR_LITERAL, contexto.getLexema(), linea));
    }
}
