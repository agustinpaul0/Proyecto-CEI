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
public abstract sealed class TipoDeclarado permits Clase, Interfaz {

    private final Token token;
    private Optional<Token> parametroDeTipo = Optional.empty();
    private final Map<ClaveDeMetodo, Metodo> metodosPropios = new LinkedHashMap<>();
    private Map<ClaveDeMetodo, Metodo> metodos = metodosPropios;
    private boolean consolidado;

    protected TipoDeclarado(Token token) {
        this.token = token;
    }

    public Token token() {
        return token;
    }

    public String nombre() {
        return token.lexema();
    }

    public void setParametroDeTipo(Optional<Token> parametro) {
        this.parametroDeTipo = parametro;
    }

    public Optional<Token> parametroDeTipo() {
        return parametroDeTipo;
    }

    public boolean esGenerico() {
        return parametroDeTipo.isPresent();
    }

    public boolean declaraParametroDeTipo(String nombre) {
        return parametroDeTipo.map(Token::lexema).filter(nombre::equals).isPresent();
    }

    public void agregarMetodo(Metodo metodo) {
        if (metodosPropios.containsKey(metodo.clave())) {
            TablaDeSimbolos.registrarRepetido(new ExcepcionSemantica(metodo.token(),
                    "El metodo " + metodo.nombre() + " con " + metodo.aridad()
                            + " parametros ya esta declarado en " + nombre()));
        } else {
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

    protected void heredarMetodos(TipoDeclarado ancestro, Sustitucion sustitucion,
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
        void controlar(Metodo propio, Metodo heredado, TipoDeclarado ancestro) throws ExcepcionSemantica;
    }

    protected static Sustitucion sustitucionHacia(TipoReferencia referencia, TipoDeclarado ancestro) {
        if (ancestro.parametroDeTipo.isEmpty() || referencia.argumento().isEmpty()) {
            return Sustitucion.identidad();
        }
        return Sustitucion.de(ancestro.parametroDeTipo.get().lexema(), referencia.argumento().get());
    }

    // El ciclo se reporta en el nombre de este tipo, la entidad con el problema
    // (Pautas slide 30). Un ciclo más arriba que no pasa por este tipo lo reporta
    // el primero de ese ciclo, cuando le toca su chequeo.
    protected void verificarQueNoHayaCiclo() throws ExcepcionSemantica {
        HashSet<TipoDeclarado> visitados = new HashSet<>();
        Optional<TipoDeclarado> actual = ancestroDirecto();
        while (actual.isPresent() && visitados.add(actual.get())) {
            if (actual.get() == this) {
                throw new ExcepcionSemantica(token, "Herencia circular: " + nombre()
                        + " es ancestro de si mismo");
            }
            actual = actual.get().ancestroDirecto();
        }
    }

    // Un ancestro no declarado da vacío, sin error: lo reporta el chequeo de la herencia.
    protected abstract Optional<TipoDeclarado> ancestroDirecto();
}
