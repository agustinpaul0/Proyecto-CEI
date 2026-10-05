package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;

public record Atributo(Token token, Tipo tipo) implements Miembro {

    public String nombre() {
        return token.lexema();
    }

    void estaBienDeclarado(ClaseOInterfaz contexto) throws ExcepcionSemantica {
        tipo.verificarEn(contexto, false);
    }

    Atributo instanciado(Sustitucion sustitucion) {
        return new Atributo(token, tipo.instanciar(sustitucion));
    }
}
