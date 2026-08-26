package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.PosicionLexica;
import minijava.lexico.ResultadoDeTransicion;

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
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.COMENTARIO_SIN_CERRAR, "/*", posicionDeApertura);
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
