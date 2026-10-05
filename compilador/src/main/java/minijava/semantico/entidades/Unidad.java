package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;

import java.util.ArrayList;
import java.util.List;

// En la Etapa 4 es lo que tiene cuerpo: de acá cuelga lo que se agregue.
public abstract sealed class Unidad permits Metodo, Constructor {

    private final Token token;
    private final List<Parametro> parametros;

    protected Unidad(Token token) {
        this(token, new ArrayList<>());
    }

    protected Unidad(Token token, List<Parametro> parametros) {
        this.token = token;
        this.parametros = new ArrayList<>(parametros);
    }

    public Token token() {
        return token;
    }

    public String nombre() {
        return token.lexema();
    }

    public List<Parametro> parametros() {
        return List.copyOf(parametros);
    }

    public int aridad() {
        return parametros.size();
    }

    public void agregarParametro(Token nombre, Tipo tipo) {
        boolean repetido = parametros.stream().anyMatch(p -> p.nombre().equals(nombre.lexema()));
        if (repetido) {
            TablaDeSimbolos.registrarRepetido(new ExcepcionSemantica(nombre,
                    "El parametro " + nombre.lexema() + " ya esta declarado en " + nombre()));
        } else {
            parametros.add(new Parametro(nombre, tipo, parametros.size()));
        }
    }

    protected void verificarParametrosEn(ClaseOInterfaz contexto, boolean enContextoEstatico)
            throws ExcepcionSemantica {
        for (Parametro parametro : parametros) {
            parametro.tipo().verificarEn(contexto, enContextoEstatico);
        }
    }
}
