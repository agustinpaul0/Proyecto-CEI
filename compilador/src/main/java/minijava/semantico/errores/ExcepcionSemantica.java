package minijava.semantico.errores;

import minijava.lexico.tokens.Token;

// El token de cada clase de error lo fija la Pautas Etapa 3, slide 30.
public final class ExcepcionSemantica extends Exception {

    private final Token token;

    public ExcepcionSemantica(Token token, String descripcion) {
        super("Error Semantico en linea " + token.linea() + ": " + descripcion);
        this.token = token;
    }

    public String getLexemaOfensivo() {
        return token.lexema();
    }

    public int getLinea() {
        return token.linea();
    }
}
