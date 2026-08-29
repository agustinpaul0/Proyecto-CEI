package minijava.lexico.automata;

import minijava.tokens.TablaDeLexemas;
import minijava.tokens.Token;

public record Emitir(Token token) implements ResultadoDeTransicion {

    // Atajo para los 49 tokens de lexema fijo: el autómata ya delimitó el lexema
    // y la tabla lo nombra, así que el estado no necesita llevar el tipo encima.
    public static Emitir porLexemaFijo(String lexema, int linea) {
        return new Emitir(new Token(TablaDeLexemas.tipoDelLexemaFijo(lexema), lexema, linea));
    }
}
