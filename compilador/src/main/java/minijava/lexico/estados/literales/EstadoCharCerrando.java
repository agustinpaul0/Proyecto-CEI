package minijava.lexico.estados.literales;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;

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
            throw contexto.crearExcepcionLexica(seInterrumpio
                            ? TipoDeErrorLexico.CHAR_SIN_CERRAR
                            : TipoDeErrorLexico.CHAR_MAL_FORMADO,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        int linea = contexto.getLinea();
        contexto.avanzar();
        return new Emitir(new Token(TipoDeToken.CHAR_LITERAL, contexto.getLexema(), linea));
    }
}
