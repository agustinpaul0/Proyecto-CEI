package minijava.lexico;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.estados.EstadoInicial;
import minijava.lexico.tokens.Token;

public final class AnalizadorLexico {

    private final ContextoLexico contexto;

    public AnalizadorLexico(ContextoLexico contexto) {
        this.contexto = contexto;
    }

    public AnalizadorLexico(ManejadorDeArchivo manejador) {
        this(new ContextoLexico(manejador));
    }

    // Driver iterativo: mismo AF que encadena la cátedra, sin su profundidad de
    // pila. No hay paso de recuperación
    // (documentacion/compartido/manejo-de-errores.md).
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
