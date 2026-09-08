package minijava.lexico.estados.operadores;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;

// && y ||: el segundo carácter es obligatorio y repite al primero. '&' y '|'
// sueltos son error, no están entre los operadores de Aspectos Léxicos §2.4.2.
// No terminal, a diferencia de EstadoOperadorConSufijoOpcional.
public final class EstadoOperadorDoble implements EstadoLexico {

    public static final EstadoOperadorDoble INSTANCIA = new EstadoOperadorDoble();

    private EstadoOperadorDoble() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        int linea = contexto.getLinea();
        char primero = contexto.getLexema().charAt(0);
        if (contexto.getCaracterActual() != primero) {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.OPERADOR_INCOMPLETO,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        contexto.avanzar();
        return Emitir.porLexemaFijo(contexto.getLexema(), linea);
    }
}
