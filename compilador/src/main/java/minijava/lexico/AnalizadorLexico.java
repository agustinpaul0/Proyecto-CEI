package minijava.lexico;

import minijava.config.ConfiguracionDelCompilador;
import minijava.errores.ExcepcionLexica;
import minijava.io.ManejadorDeArchivo;
import minijava.lexico.estados.EstadoInicial;
import minijava.tokens.Token;

public final class AnalizadorLexico {

    private final ContextoLexico contexto;

    public AnalizadorLexico(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    public AnalizadorLexico(ManejadorDeArchivo manejador) {
        this(manejador, ConfiguracionDelCompilador.DEFAULT);
    }

    public AnalizadorLexico(ManejadorDeArchivo manejador, ConfiguracionDelCompilador configuracion) {
        this(new ContextoLexico(manejador, configuracion));
    }

    // Driver iterativo del patrón State: mismo AF que la cátedra enseña
    // encadenando llamadas, sin la profundidad de pila del encadenamiento
    // recursivo (ver agents/arquitectura.md). No hay paso de recuperación —
    // el criterio está en agents/manejo-de-errores.md.
    public Token proximoToken() throws ExcepcionLexica {
        contexto.iniciarNuevoToken();
        EstadoLexico estado = EstadoInicial.INSTANCIA;
        while (true) {
            ResultadoDeTransicion resultado = estado.procesar(contexto);
            switch (resultado) {
                case Emitir emitir -> {
                    return emitir.token();
                }
                case Continuar continuar -> estado = continuar.siguiente();
            }
        }
    }
}
