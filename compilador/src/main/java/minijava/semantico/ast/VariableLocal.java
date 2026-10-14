package minijava.semantico.ast;

import minijava.lexico.tokens.Token;
import minijava.semantico.tipos.Tipo;

// El bloque al que pertenece es el mapa de Contexto que la guarda.
public record VariableLocal(Token token, Tipo tipo) {
}
