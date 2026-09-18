package minijava.lexico.estados.operadores;

import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;

// Los seis pares con sufijo opcional: >/>=, </<=, !/!=, =/==, +/++, -/--.
// Terminal: si el sufijo no viene, emite el token corto sin consumir el lookahead.
//
// Tres instancias y no seis: lo único que distinguiría a '>' de '<' es el tipo de
// token, y ése lo da la tabla a partir del lexema armado.
public final class EstadoOperadorConSufijoOpcional implements EstadoLexico {

    public static final EstadoOperadorConSufijoOpcional SUFIJO_IGUAL =
            new EstadoOperadorConSufijoOpcional('=');
    public static final EstadoOperadorConSufijoOpcional SUFIJO_MAS =
            new EstadoOperadorConSufijoOpcional('+');
    public static final EstadoOperadorConSufijoOpcional SUFIJO_MENOS =
            new EstadoOperadorConSufijoOpcional('-');

    private final char sufijo;

    private EstadoOperadorConSufijoOpcional(char sufijo) {
        this.sufijo = sufijo;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        int linea = contexto.getLinea();
        if (contexto.getCaracterActual() == sufijo) {
            contexto.avanzar();
        }
        return Emitir.porLexemaFijo(contexto.getLexema(), linea);
    }
}
