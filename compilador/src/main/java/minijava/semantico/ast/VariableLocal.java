package minijava.semantico.ast;

import minijava.lexico.tokens.Token;
import minijava.semantico.tipos.Tipo;

// El bloque al que pertenece es el mapa de Contexto que la guarda. `offset` es su
// desplazamiento en el registro de activación (Apunte §8): 0, -1, -2, … según las
// locales vivas al declararla.
public record VariableLocal(Token token, Tipo tipo, int offset) {
}
