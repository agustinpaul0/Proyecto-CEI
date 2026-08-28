package minijava.lexico.estados.comentarios;

import minijava.errores.ExcepcionLexica;
import minijava.errores.PosicionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.estados.EstadoInicial;

public final class EstadoComentarioMultilinea implements EstadoLexico {

    private final PosicionLexica posicionDeApertura;
    private final boolean ultimoFueAsterisco;

    public EstadoComentarioMultilinea(PosicionLexica posicionDeApertura, boolean ultimoFueAsterisco) {
        this.posicionDeApertura = posicionDeApertura;
        this.ultimoFueAsterisco = ultimoFueAsterisco;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.esFinDeArchivo()) {
            throw contexto.crearExcepcionLexicaEn(TipoDeErrorLexico.COMENTARIO_SIN_CERRAR, "/*",
                    posicionDeApertura);
        }
        char actual = contexto.getCaracterActual();
        if (ultimoFueAsterisco && actual == '/') {
            contexto.descartar();
            return new Continuar(EstadoInicial.INSTANCIA);
        }
        contexto.descartar();
        return new Continuar(new EstadoComentarioMultilinea(posicionDeApertura, actual == '*'));
    }
}
