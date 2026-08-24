package minijava.lexico;

import minijava.errores.ExcepcionLexica;
import minijava.io.ManejadorDeArchivo;
import minijava.lexico.estados.EstadoInicial;
import minijava.tokens.Token;

public final class AnalizadorLexico {

    private final ContextoLexico contexto;

    public AnalizadorLexico(ManejadorDeArchivo manejador) {
        this.contexto = new ContextoLexico(manejador);
    }

    // Driver iterativo del patrón State: mismo AF que la cátedra enseña
    // encadenando llamadas, sin la profundidad de pila del encadenamiento
    // recursivo (ver AGENTS.md §4).
    public Token proximoToken() throws ExcepcionLexica {
        contexto.iniciarNuevoToken();
        EstadoLexico estado = EstadoInicial.INSTANCIA;
        while (true) {
            ResultadoDeTransicion resultado = estado.procesar(contexto);
            // switch exhaustivo sobre la interfaz sellada (Continuar | Emitir):
            // el compilador exige cubrir ambos permits, sin default ni casteo.
            switch (resultado) {
                case Emitir emitir -> {
                    return emitir.token();
                }
                case Continuar continuar -> estado = continuar.siguiente();
            }
        }
    }

    // Logro Multi-detección de Errores: avanza al menos un carácter (o llega
    // a EOF) para que un reintento de proximoToken() no repita el mismo
    // error indefinidamente — garantiza terminación de la recuperación.
    public void recuperarDeError() {
        if (!contexto.esFinDeArchivo()) {
            contexto.descartar();
        }
    }
}
