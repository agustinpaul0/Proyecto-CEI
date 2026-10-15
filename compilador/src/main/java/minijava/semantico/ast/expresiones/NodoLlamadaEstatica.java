package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.entidades.ClaveDeMetodo;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;

import java.util.List;

// `Clase.m(e1, ..., en)`. Documento §10.2: la clase existe y el método es
// estático. Lo que falla en la clase va en su nombre; un método que no existe o
// no es estático, en el punto, como en javac.
public final class NodoLlamadaEstatica extends NodoExpresion {

    private final Token clase;
    private final Token punto;
    private final Token nombre;
    private final Token parentesis;
    private final List<NodoExpresion> argumentos;

    public NodoLlamadaEstatica(Token clase, Token punto, Token nombre, Token parentesis,
                               List<NodoExpresion> argumentos) {
        this.clase = clase;
        this.punto = punto;
        this.nombre = nombre;
        this.parentesis = parentesis;
        this.argumentos = List.copyOf(argumentos);
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        ClaseOInterfaz declarada = TablaDeSimbolos.tipo(clase.lexema()).orElseThrow(() ->
                new ExcepcionSemantica(clase, "La clase " + clase.lexema() + " no esta declarada"));
        if (!(declarada instanceof Clase)) {
            throw new ExcepcionSemantica(clase, clase.lexema()
                    + " es una interfaz: sus metodos no se llaman con el nombre del tipo");
        }
        ClaveDeMetodo clave = new ClaveDeMetodo(nombre.lexema(), argumentos.size());
        Metodo metodo = declarada.metodo(clave).orElseThrow(() -> new ExcepcionSemantica(punto,
                "El metodo " + clave + " no esta declarado en " + clase.lexema()));
        if (!metodo.esEstatico()) {
            throw new ExcepcionSemantica(punto, "El metodo " + clave
                    + " es de instancia y no se puede llamar con el nombre de la clase");
        }
        Argumentos.chequear(argumentos, metodo.parametros(), Sustitucion.identidad(), nombre, contexto);
        return metodo.retorno();
    }

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
        return "(llamar " + clase.lexema() + "." + nombre.lexema() + Argumentos.comoTexto(argumentos) + ")";
    }
}
