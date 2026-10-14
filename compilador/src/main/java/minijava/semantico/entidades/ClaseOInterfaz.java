package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.TipoReferencia;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Clases e interfaces comparten la tabla global de tipos (documento §4).
//
// `metodosPropios` son los declarados en el fuente; `metodos` son los de la
// tabla consolidada, que suma los heredados. Hasta consolidar coinciden.
public abstract sealed class ClaseOInterfaz permits Clase, Interfaz {

    private final Token token;
    private Token parametroDeTipo;
    private final Map<ClaveDeMetodo, Metodo> metodosPropios = new LinkedHashMap<>();
    private Map<ClaveDeMetodo, Metodo> metodos = metodosPropios;
    private boolean consolidado;

    protected ClaseOInterfaz(Token token) {
        this.token = token;
    }

    public Token token() {
        return token;
    }

    public String nombre() {
        return token.lexema();
    }

    public void setParametroDeTipo(Token parametro) {
        this.parametroDeTipo = parametro;
    }

    public Optional<Token> parametroDeTipo() {
        return Optional.ofNullable(parametroDeTipo);
    }

    public boolean esGenerico() {
        return parametroDeTipo != null;
    }

    public boolean declaraParametroDeTipo(String nombre) {
        return esGenerico() && parametroDeTipo.lexema().equals(nombre);
    }

    public void agregarMetodo(Metodo metodo) {
        if (metodosPropios.containsKey(metodo.clave())) {
            TablaDeSimbolos.registrarRepetido(new ExcepcionSemantica(metodo.token(),
                    "El metodo " + metodo.nombre() + " con " + metodo.aridad()
                            + " parametros ya esta declarado en " + nombre()));
        } else {
            metodo.setDeclarante(this);
            metodosPropios.put(metodo.clave(), metodo);
        }
    }

    public Optional<Metodo> metodo(ClaveDeMetodo clave) {
        return Optional.ofNullable(metodos.get(clave));
    }

    public List<Metodo> metodos() {
        return List.copyOf(metodos.values());
    }

    protected List<Metodo> metodosPropios() {
        return List.copyOf(metodosPropios.values());
    }

    protected boolean esPropio(Metodo metodo) {
        return metodosPropios.get(metodo.clave()) == metodo;
    }

    @Override
    public String toString() {
        return nombre();
    }

    public abstract void estaBienDeclarado() throws ExcepcionSemantica;

    // Delegación y recursión (Clase 8, slide 26): un tipo se consolida sólo después
    // de su ancestro. El chequeo ya descartó los ciclos, así que la recursión termina.
    public final void consolidar() throws ExcepcionSemantica {
        if (consolidado) {
            return;
        }
        consolidado = true;
        consolidarConAncestros();
    }

    protected abstract void consolidarConAncestros() throws ExcepcionSemantica;

    protected void heredarMetodos(ClaseOInterfaz ancestro, Sustitucion sustitucion,
                                  ControlDeRedefinicion controlDeRedefinicion) throws ExcepcionSemantica {
        Map<ClaveDeMetodo, Metodo> consolidados = new LinkedHashMap<>();
        for (Metodo heredado : ancestro.metodos.values()) {
            consolidados.put(heredado.clave(), heredado.instanciado(sustitucion));
        }
        for (Metodo propio : metodosPropios.values()) {
            Metodo heredado = consolidados.get(propio.clave());
            if (heredado != null) {
                controlDeRedefinicion.controlar(propio, heredado, ancestro);
            }
            consolidados.put(propio.clave(), propio);
        }
        metodos = consolidados;
    }

    @FunctionalInterface
    protected interface ControlDeRedefinicion {
        void controlar(Metodo propio, Metodo heredado, ClaseOInterfaz ancestro) throws ExcepcionSemantica;
    }

    // Cómo se ven los miembros de `declarado` a través de `referencia`: con
    // `Caja<String>`, la T de Caja es String (documento §5 y §5.1).
    public static Sustitucion sustitucionHacia(TipoReferencia referencia, ClaseOInterfaz declarado) {
        if (!declarado.esGenerico() || referencia.argumento().isEmpty()) {
            return Sustitucion.identidad();
        }
        return Sustitucion.de(declarado.nombre(), declarado.parametroDeTipo.lexema(),
                referencia.argumento().get());
    }

    // La clase padre y la interfaz implementada, o la interfaz extendida, tal como
    // se escribieron en el encabezado.
    public abstract List<TipoReferencia> ancestrosDirectos();

    // El ciclo se reporta en el nombre de este tipo, la entidad con el problema
    // (Pautas slide 30). Un ciclo más arriba que no pasa por este tipo lo reporta
    // el primero de ese ciclo, cuando le toca su chequeo.
    protected void verificarQueNoHayaCiclo() throws ExcepcionSemantica {
        HashSet<ClaseOInterfaz> visitados = new HashSet<>();
        Optional<ClaseOInterfaz> actual = ancestroDirecto();
        while (actual.isPresent() && visitados.add(actual.get())) {
            if (actual.get() == this) {
                throw new ExcepcionSemantica(token, "Herencia circular: " + nombre()
                        + " es ancestro de si mismo");
            }
            actual = actual.get().ancestroDirecto();
        }
    }

    // Un ancestro no declarado da vacío, sin error: lo reporta el chequeo de la herencia.
    protected abstract Optional<ClaseOInterfaz> ancestroDirecto();
}
