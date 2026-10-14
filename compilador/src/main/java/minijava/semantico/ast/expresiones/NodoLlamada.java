package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.ClaveDeMetodo;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;

import java.util.List;

// `m(e1, ..., en)`, sin receptor. Documento §10.2: se busca (m, n) en la clase
// actual, con los heredados; en un método estático tiene que ser estático.
public final class NodoLlamada extends NodoExpresion {

    private final Token nombre;
    private final Token parentesis;
    private final List<NodoExpresion> argumentos;

    public NodoLlamada(Token nombre, Token parentesis, List<NodoExpresion> argumentos) {
        this.nombre = nombre;
        this.parentesis = parentesis;
        this.argumentos = List.copyOf(argumentos);
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        ClaveDeMetodo clave = new ClaveDeMetodo(nombre.lexema(), argumentos.size());
        Metodo metodo = contexto.clase().metodo(clave).orElseThrow(() -> new ExcepcionSemantica(nombre,
                "El metodo " + clave + " no esta declarado en " + contexto.clase().nombre()));
        if (contexto.esEstatico() && !metodo.esEstatico()) {
            throw new ExcepcionSemantica(nombre, "El metodo de instancia " + clave
                    + " no se puede llamar sin receptor en un metodo estatico");
        }
        Argumentos.chequearEnLaLlamada(argumentos, metodo.parametros(), Sustitucion.identidad(), nombre, contexto);
        return metodo.retorno();
    }

    // javac ubica en el `(` el error de una llamada cuyo resultado no sirve donde
    // se usa; los de la resolución, en el nombre.
    @Override
    public Token token() {
        return parentesis;
    }

    @Override
    public boolean esLlamada() {
        return true;
    }

    @Override
    public String toString() {
        return "(llamar " + nombre.lexema() + Argumentos.comoTexto(argumentos) + ")";
    }
}
