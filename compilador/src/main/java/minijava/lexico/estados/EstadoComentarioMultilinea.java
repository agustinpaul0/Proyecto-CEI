package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;

// Descarta caracteres hasta encontrar "*/". No es un singleton: lleva la
// línea de apertura (para el mensaje de error si no cierra) y si el último
// carácter visto fue '*' (para reconocer "*/" partido entre dos consultas).
public final class EstadoComentarioMultilinea implements EstadoLexico {

    private final int lineaDeApertura;
    private final boolean ultimoFueAsterisco;

    public EstadoComentarioMultilinea(int lineaDeApertura, boolean ultimoFueAsterisco) {
        this.lineaDeApertura = lineaDeApertura;
        this.ultimoFueAsterisco = ultimoFueAsterisco;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        if (contexto.esFinDeArchivo()) {
            throw contexto.crearExcepcionLexica("/*", lineaDeApertura);
        }
        char actual = contexto.getCaracterActual();
        if (ultimoFueAsterisco && actual == '/') {
            contexto.descartar();
            return new Continuar(EstadoInicial.INSTANCIA);
        }
        contexto.descartar();
        return new Continuar(new EstadoComentarioMultilinea(lineaDeApertura, actual == '*'));
    }
}
