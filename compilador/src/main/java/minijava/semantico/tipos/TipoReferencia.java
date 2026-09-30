package minijava.semantico.tipos;

import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.TipoDeclarado;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;

import java.util.Optional;

// `Caja`, `Caja<T>` o `Caja<String>`. El argumento es un parámetro de tipo o
// una referencia; si es una referencia genérica (`Caja<Par<String>>`), de ella
// se controla sólo que exista: el anidamiento es el logro Genericidad Avanzada
// E3, que no se persigue.
public record TipoReferencia(Token token, Optional<Tipo> argumento) implements Tipo {

    public TipoReferencia(Token token) {
        this(token, Optional.empty());
    }

    @Override
    public String nombre() {
        return argumento.map(a -> token.lexema() + "<" + a.nombre() + ">").orElse(token.lexema());
    }

    public String nombreDelTipo() {
        return token.lexema();
    }

    @Override
    public void verificarEn(TipoDeclarado contexto, boolean enContextoEstatico) throws ExcepcionSemantica {
        TipoDeclarado declarado = declaracion();
        if (argumento.isEmpty()) {
            return;
        }
        if (!declarado.esGenerico()) {
            throw new ExcepcionSemantica(token, "El tipo " + nombreDelTipo()
                    + " no es generico y no admite argumento de tipo");
        }
        switch (argumento.get()) {
            case TipoReferencia referencia -> referencia.declaracion();
            case Tipo otro -> otro.verificarEn(contexto, enContextoEstatico);
        }
    }

    public TipoDeclarado declaracion() throws ExcepcionSemantica {
        Optional<TipoDeclarado> declarado = TablaDeSimbolos.tipo(nombreDelTipo());
        if (declarado.isEmpty()) {
            throw new ExcepcionSemantica(token, "La clase o interfaz " + nombreDelTipo() + " no esta declarada");
        }
        return declarado.get();
    }

    @Override
    public Tipo instanciar(Sustitucion sustitucion) {
        return new TipoReferencia(token, argumento.map(a -> a.instanciar(sustitucion)));
    }
}
