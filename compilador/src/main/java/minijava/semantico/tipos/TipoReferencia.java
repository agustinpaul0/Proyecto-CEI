package minijava.semantico.tipos;

import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;

import java.util.Optional;

// `Caja`, `Caja<T>` o `Caja<String>`. El argumento es un parámetro de tipo o
// una referencia; si es una referencia genérica (`Caja<Par<String>>`), de ella
// se controla sólo que exista: el anidamiento es el logro Genericidad Avanzada
// E3, que no se persigue.
public final class TipoReferencia implements Tipo {

    private final Token token;
    private final Tipo argumento;

    public TipoReferencia(Token token) {
        this(token, null);
    }

    public TipoReferencia(Token token, Tipo argumento) {
        this.token = token;
        this.argumento = argumento;
    }

    public Token token() {
        return token;
    }

    public Optional<Tipo> argumento() {
        return Optional.ofNullable(argumento);
    }

    @Override
    public String nombre() {
        return argumento().map(a -> token.lexema() + "<" + a.nombre() + ">").orElse(token.lexema());
    }

    public String nombreDelTipo() {
        return token.lexema();
    }

    @Override
    public void verificarEn(ClaseOInterfaz contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
        ClaseOInterfaz declarado = declaracion();
        if (argumento == null) {
            return;
        }
        if (!declarado.esGenerico()) {
            throw new ExcepcionSemantica(token, "El tipo " + nombreDelTipo()
                    + " no es generico y no admite argumento de tipo");
        }
        switch (argumento) {
            case TipoReferencia referencia -> referencia.declaracion();
            case Tipo otro -> otro.verificarEn(contexto, enContextoEstatico);
        }
    }

    public ClaseOInterfaz declaracion() throws ExcepcionSemantica {
        Optional<ClaseOInterfaz> declarado = TablaDeSimbolos.tipo(nombreDelTipo());
        if (declarado.isEmpty()) {
            throw new ExcepcionSemantica(token, "La clase o interfaz " + nombreDelTipo() + " no esta declarada");
        }
        return declarado.get();
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return argumento == null ? this : new TipoReferencia(token, argumento.instanciar(sustitucion));
    }
}
