package minijava.semantico.entidades;

import minijava.generacion.Generador;
import minijava.generacion.Predefinidos;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.ast.sentencias.NodoBloque;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;

import java.util.ArrayList;
import java.util.List;

// Un método o un constructor: lo que tiene cuerpo. `declarante` es la clase o
// interfaz que lo declara, también en las copias que deja la consolidación.
public abstract sealed class Unidad permits Metodo, Constructor {

    private final Token token;
    private final List<Parametro> parametros;
    private ClaseOInterfaz declarante;
    private NodoBloque cuerpo;

    protected Unidad(Token token) {
        this(token, new ArrayList<>());
    }

    protected Unidad(Token token, List<Parametro> parametros) {
        this.token = token;
        this.parametros = new ArrayList<>(parametros);
    }

    protected Unidad(Token token, List<Parametro> parametros, ClaseOInterfaz declarante, NodoBloque cuerpo) {
        this(token, parametros);
        this.declarante = declarante;
        this.cuerpo = cuerpo;
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

    void setDeclarante(ClaseOInterfaz declarante) {
        this.declarante = declarante;
    }

    public ClaseOInterfaz declarante() {
        return declarante;
    }

    public void setCuerpo(NodoBloque cuerpo) {
        this.cuerpo = cuerpo;
    }

    public NodoBloque cuerpo() {
        return cuerpo;
    }

    // El constructor por defecto y los métodos de las predefinidas no tienen cuerpo.
    void chequearCuerpo(Clase clase) throws ExcepcionSemantica {
        if (cuerpo != null) {
            cuerpo.chequear(new Contexto(clase, this));
        }
    }

    // Un método de instancia o un constructor: tiene `this` en el desplazamiento 3.
    public abstract boolean esDeInstancia();

    public abstract String etiqueta();

    private int celdaDeThis() {
        return esDeInstancia() ? 1 : 0;
    }

    // Apunte §10, tabla de la p. 19: con n parámetros, el i-ésimo (desde 1) está en
    // n+3-i, o en n+4-i si hay `this`. `posicion` cuenta desde 0.
    public int offsetDeParametro(int posicion) {
        return aridad() + 2 - posicion + celdaDeThis();
    }

    public int offsetDeRetorno() {
        return aridad() + 3 + celdaDeThis();
    }

    public int celdasALiberar() {
        return aridad() + celdaDeThis();
    }

    // Apunte §8 y §10: prólogo, cuerpo y fin. El constructor por defecto y los
    // métodos predefinidos no tienen cuerpo en el AST.
    public void generar(Generador g) {
        g.etiqueta(etiqueta());
        g.gen("LOADFP");
        g.gen("LOADSP");
        g.gen("STOREFP");
        if (cuerpo == null) {
            Predefinidos.generarCuerpo(g, declarante.nombre(), nombre(), aridad());
        } else {
            cuerpo.generar(g);
        }
        generarFin(g);
    }

    // El final de la unidad, que también usa cada `return` después de liberar sus
    // locales. Un constructor deja `this` como resultado del `new` (Apunte §11).
    public void generarFin(Generador g) {
        if (this instanceof Constructor) {
            g.gen("LOAD 3");
            g.gen("STORE " + offsetDeRetorno());
        }
        g.gen("STOREFP");
        g.gen("RET " + celdasALiberar());
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
