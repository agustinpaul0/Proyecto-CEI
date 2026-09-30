package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;

public record Parametro(Token token, Tipo tipo, int posicion) {

    public String nombre() {
        return token.lexema();
    }

    Parametro instanciado(Sustitucion sustitucion) {
        return new Parametro(token, tipo.instanciar(sustitucion), posicion);
    }
}
