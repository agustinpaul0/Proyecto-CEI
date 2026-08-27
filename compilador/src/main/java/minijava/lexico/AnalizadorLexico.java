package minijava.lexico;

import minijava.config.ConfiguracionDelCompilador;
import minijava.errores.ExcepcionLexica;
import minijava.io.ManejadorDeArchivo;
import minijava.lexico.estados.EstadoInicial;
import minijava.tokens.Token;

import java.util.List;

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

    // Errores detectados durante el último proximoToken() que no impidieron
    // reconocerlo. El cliente los drena y los reporta; el token es válido.
    public List<ExcepcionLexica> tomarErroresNoFatales() {
        return contexto.tomarErroresNoFatales();
    }

    // Logro Multi-detección de Errores: NO hay paso de recuperación. El
    // criterio es que todo estado que lanza un error consume del fuente
    // exactamente los caracteres que reporta en el lexema, así que un
    // reintento de proximoToken() arranca en el primer carácter que no formó
    // parte de ese lexema y el análisis avanza solo. La terminación queda
    // garantizada porque el lexema de un error nunca es vacío.
    //
    // Driver iterativo del patrón State: mismo AF que la cátedra enseña
    // encadenando llamadas, sin la profundidad de pila del encadenamiento
    // recursivo (ver agents/arquitectura.md).
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
