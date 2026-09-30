package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.TipoReferencia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Clase extends TipoDeclarado {

    private Optional<TipoReferencia> padre = Optional.empty();
    private Optional<TipoReferencia> interfaz = Optional.empty();
    private final Map<String, Atributo> atributosPropios = new LinkedHashMap<>();
    private Map<String, Atributo> atributos = atributosPropios;
    private final Map<Integer, Constructor> constructores = new LinkedHashMap<>();
    private boolean esFinal;

    public Clase(Token token) {
        super(token);
    }

    public void setPadre(TipoReferencia padre) {
        this.padre = Optional.of(padre);
    }

    public void setInterfaz(TipoReferencia interfaz) {
        this.interfaz = Optional.of(interfaz);
    }

    public Optional<TipoReferencia> padre() {
        return padre;
    }

    public Optional<TipoReferencia> interfaz() {
        return interfaz;
    }

    // String y System son `final` en Java; el documento no dice nada, y rige Java.
    public void hacerFinal() {
        esFinal = true;
    }

    public void agregarMiembro(Miembro miembro) {
        switch (miembro) {
            case Atributo atributo -> agregarAtributo(atributo);
            case Metodo metodo -> agregarMetodo(metodo);
            case Constructor constructor -> agregarConstructor(constructor);
        }
    }

    public void agregarAtributo(Atributo atributo) {
        if (atributosPropios.containsKey(atributo.nombre())) {
            TablaDeSimbolos.registrarRepetido(new ExcepcionSemantica(atributo.token(),
                    "El atributo " + atributo.nombre() + " ya esta declarado en " + nombre()));
        } else {
            atributosPropios.put(atributo.nombre(), atributo);
        }
    }

    public void agregarConstructor(Constructor constructor) {
        if (constructores.containsKey(constructor.aridad())) {
            TablaDeSimbolos.registrarRepetido(new ExcepcionSemantica(constructor.token(),
                    "El constructor de " + nombre() + " con " + constructor.aridad()
                            + " parametros ya esta declarado"));
        } else {
            constructores.put(constructor.aridad(), constructor);
        }
    }

    // Documento §6.3: si la clase no declara constructores, se agrega uno sin
    // parámetros y con cuerpo vacío.
    public void cerrar() {
        if (constructores.isEmpty()) {
            constructores.put(0, new Constructor(token()));
        }
    }

    public Optional<Atributo> atributo(String nombre) {
        return Optional.ofNullable(atributos.get(nombre));
    }

    public List<Atributo> atributos() {
        return List.copyOf(atributos.values());
    }

    public List<Constructor> constructores() {
        return List.copyOf(constructores.values());
    }

    @Override
    public void estaBienDeclarado() throws ExcepcionSemantica {
        if (padre.isPresent()) {
            verificarPadre(padre.get());
        }
        if (interfaz.isPresent()) {
            verificarInterfaz(interfaz.get());
        }
        verificarQueNoHayaCiclo();
        for (Atributo atributo : atributosPropios.values()) {
            atributo.estaBienDeclarado(this);
        }
        for (Constructor constructor : constructores.values()) {
            constructor.estaBienDeclarado(this);
        }
        for (Metodo metodo : metodosPropios()) {
            metodo.estaBienDeclarado(this);
        }
    }

    private void verificarPadre(TipoReferencia referencia) throws ExcepcionSemantica {
        referencia.verificarEn(this, false);
        switch (referencia.declaracion()) {
            case Interfaz interfaz -> throw new ExcepcionSemantica(referencia.token(), "La clase "
                    + nombre() + " no puede extender la interfaz " + interfaz.nombre()
                    + ": una clase extiende una clase y implementa una interfaz");
            case Clase clase when clase.esFinal -> throw new ExcepcionSemantica(referencia.token(),
                    "La clase " + nombre() + " no puede heredar de la clase final " + clase.nombre());
            case Clase clase -> {
            }
        }
    }

    private void verificarInterfaz(TipoReferencia referencia) throws ExcepcionSemantica {
        referencia.verificarEn(this, false);
        TipoDeclarado declarado = referencia.declaracion();
        if (declarado instanceof Clase) {
            throw new ExcepcionSemantica(referencia.token(), "La clase " + nombre()
                    + " no puede implementar la clase " + declarado.nombre()
                    + ": solo se implementan interfaces");
        }
    }

    @Override
    protected Optional<TipoDeclarado> ancestroDirecto() {
        return padre.flatMap(p -> TablaDeSimbolos.tipo(p.nombreDelTipo()));
    }

    @Override
    protected void consolidarConAncestros() throws ExcepcionSemantica {
        if (padre.isPresent()) {
            Clase ancestro = (Clase) padre.get().declaracion();
            ancestro.consolidar();
            Sustitucion sustitucion = sustitucionHacia(padre.get(), ancestro);
            heredarAtributos(ancestro, sustitucion);
            heredarMetodos(ancestro, sustitucion, Clase::controlarRedefinicion);
        }
        if (interfaz.isPresent()) {
            Interfaz implementada = (Interfaz) interfaz.get().declaracion();
            implementada.consolidar();
            verificarImplementacion(implementada, sustitucionHacia(interfaz.get(), implementada));
        }
    }

    private void heredarAtributos(Clase ancestro, Sustitucion sustitucion) throws ExcepcionSemantica {
        Map<String, Atributo> consolidados = new LinkedHashMap<>();
        for (Atributo heredado : ancestro.atributos.values()) {
            consolidados.put(heredado.nombre(), heredado.instanciado(sustitucion));
        }
        for (Atributo propio : atributosPropios.values()) {
            if (consolidados.containsKey(propio.nombre())) {
                throw new ExcepcionSemantica(propio.token(), "El atributo " + propio.nombre()
                        + " ya esta declarado en un ancestro de " + nombre());
            }
            consolidados.put(propio.nombre(), propio);
        }
        atributos = consolidados;
    }

    // Documento §6.2: los métodos estáticos no participan de la redefinición, y la
    // de instancia exige exactamente la misma signatura.
    private static void controlarRedefinicion(Metodo propio, Metodo heredado, TipoDeclarado ancestro)
            throws ExcepcionSemantica {
        if (heredado.esEstatico()) {
            throw new ExcepcionSemantica(propio.token(), "El metodo " + propio.nombre()
                    + " no puede redeclarar el metodo estatico " + heredado.clave()
                    + " que hereda de " + ancestro.nombre());
        }
        if (propio.esEstatico()) {
            throw new ExcepcionSemantica(propio.token(), "El metodo estatico " + propio.nombre()
                    + " no puede redeclarar el metodo de instancia " + heredado.clave()
                    + " que hereda de " + ancestro.nombre());
        }
        if (!propio.mismaSignaturaQue(heredado)) {
            throw new ExcepcionSemantica(propio.token(), "El metodo " + propio.nombre()
                    + " redefine con otra signatura el metodo que hereda de " + ancestro.nombre());
        }
    }

    // Documento §6.2: para cada método de la interfaz hace falta un método de
    // instancia con la misma signatura, propio o heredado. Si el que falla es
    // propio, el error va en su nombre; si no, en el de la clase (Pautas slide 30).
    private void verificarImplementacion(Interfaz implementada, Sustitucion sustitucion)
            throws ExcepcionSemantica {
        for (Metodo abstracto : implementada.metodos()) {
            Metodo requerido = abstracto.instanciado(sustitucion);
            Optional<Metodo> encontrado = metodo(requerido.clave());
            if (encontrado.isEmpty()) {
                throw new ExcepcionSemantica(token(), "La clase " + nombre()
                        + " no implementa el metodo " + requerido.clave()
                        + " de la interfaz " + implementada.nombre());
            }
            Metodo implementacion = encontrado.get();
            Token culpable = esPropio(implementacion) ? implementacion.token() : token();
            if (implementacion.esEstatico()) {
                throw new ExcepcionSemantica(culpable, "El metodo estatico " + implementacion.nombre()
                        + " no puede implementar el metodo " + requerido.clave()
                        + " de la interfaz " + implementada.nombre());
            }
            if (!implementacion.mismaSignaturaQue(requerido)) {
                throw new ExcepcionSemantica(culpable, "El metodo " + implementacion.nombre()
                        + " implementa con otra signatura el metodo " + requerido.clave()
                        + " de la interfaz " + implementada.nombre());
            }
        }
    }
}
