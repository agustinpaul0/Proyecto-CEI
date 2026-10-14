package minijava.semantico.ast;

import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.Constructor;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.entidades.Parametro;
import minijava.semantico.entidades.Unidad;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoReferencia;
import minijava.semantico.tipos.VariableDeTipo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Dónde está parado el chequeo: la clase y la unidad actuales, y las variables
// locales visibles, un mapa por bloque abierto (documento §2 y §12.6).
public final class Contexto {

    private final Clase clase;
    private final Unidad unidad;
    private final Deque<Map<String, VariableLocal>> bloques = new ArrayDeque<>();

    public Contexto(Clase clase, Unidad unidad) {
        this.clase = clase;
        this.unidad = unidad;
    }

    public Clase clase() {
        return clase;
    }

    public boolean esEstatico() {
        return unidad instanceof Metodo metodo && metodo.esEstatico();
    }

    public boolean esConstructor() {
        return unidad instanceof Constructor;
    }

    // Sólo se pide dentro de un método: un constructor no tiene retorno.
    public Tipo retorno() {
        return ((Metodo) unidad).retorno();
    }

    // Documento §11.3: dentro de `Caja<T>`, `this` es de tipo `Caja<T>`.
    public TipoReferencia tipoDeThis() {
        Token nombre = clase.token();
        return new TipoReferencia(nombre, clase.parametroDeTipo()
                .map(parametro -> new VariableDeTipo(parametro, clase.nombre()))
                .orElse(null));
    }

    public void abrirBloque() {
        bloques.push(new LinkedHashMap<>());
    }

    public void cerrarBloque() {
        bloques.pop();
    }

    // Documento §12.3: una local no repite un parámetro ni una local visible, y sí
    // puede ocultar un atributo.
    public void declararLocal(Token nombre, Tipo tipo) throws ExcepcionSemantica {
        if (parametro(nombre.lexema()).isPresent()) {
            throw new ExcepcionSemantica(nombre, "La variable local " + nombre.lexema()
                    + " tiene el nombre de un parametro");
        }
        if (local(nombre.lexema()).isPresent()) {
            throw new ExcepcionSemantica(nombre, "La variable local " + nombre.lexema()
                    + " ya esta declarada");
        }
        bloques.peek().put(nombre.lexema(), new VariableLocal(nombre, tipo));
    }

    // Documento §10.1, pasos 1 y 2: primero una local visible, después un parámetro.
    public Optional<Tipo> localOParametro(String nombre) {
        return local(nombre).map(VariableLocal::tipo).or(() -> parametro(nombre).map(Parametro::tipo));
    }

    private Optional<VariableLocal> local(String nombre) {
        for (Map<String, VariableLocal> bloque : bloques) {
            if (bloque.containsKey(nombre)) {
                return Optional.of(bloque.get(nombre));
            }
        }
        return Optional.empty();
    }

    private Optional<Parametro> parametro(String nombre) {
        return unidad.parametros().stream().filter(p -> p.nombre().equals(nombre)).findFirst();
    }
}
