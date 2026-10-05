package minijava.semantico.entidades;

import minijava.lexico.tokens.Token;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.TipoReferencia;

import java.util.Optional;

public final class Interfaz extends ClaseOInterfaz {

    private TipoReferencia extendida;

    public Interfaz(Token token) {
        super(token);
    }

    public void setExtendida(TipoReferencia extendida) {
        this.extendida = extendida;
    }

    public Optional<TipoReferencia> extendida() {
        return Optional.ofNullable(extendida);
    }

    @Override
    public void estaBienDeclarado() throws ExcepcionSemantica {
        if (extendida != null) {
            extendida.verificarEn(this, false);
            ClaseOInterfaz declarado = extendida.declaracion();
            if (declarado instanceof Clase) {
                throw new ExcepcionSemantica(extendida.token(), "La interfaz " + nombre()
                        + " no puede extender la clase " + declarado.nombre()
                        + ": una interfaz solo extiende interfaces");
            }
        }
        verificarQueNoHayaCiclo();
        for (Metodo metodo : metodosPropios()) {
            metodo.estaBienDeclarado(this);
        }
    }

    @Override
    protected Optional<ClaseOInterfaz> ancestroDirecto() {
        return extendida().flatMap(e -> TablaDeSimbolos.tipo(e.nombreDelTipo()));
    }

    @Override
    protected void consolidarConAncestros() throws ExcepcionSemantica {
        if (extendida != null) {
            Interfaz ancestro = (Interfaz) extendida.declaracion();
            ancestro.consolidar();
            heredarMetodos(ancestro, sustitucionHacia(extendida, ancestro),
                    Interfaz::controlarRedefinicion);
        }
        verificarContraObject();
    }

    private static void controlarRedefinicion(Metodo propio, Metodo heredado, ClaseOInterfaz ancestro)
            throws ExcepcionSemantica {
        if (!propio.mismaSignaturaQue(heredado)) {
            throw new ExcepcionSemantica(propio.token(), "El metodo " + propio.nombre()
                    + " redefine con otra signatura el metodo de la interfaz " + ancestro.nombre());
        }
    }

    // JLS §9.2: una interfaz declara implícitamente los métodos públicos de
    // instancia de Object, así que uno propio con la misma clave tiene que tener
    // su misma signatura. Los estáticos de Object no son miembros de la interfaz.
    private void verificarContraObject() throws ExcepcionSemantica {
        ClaseOInterfaz object = TablaDeSimbolos.object();
        for (Metodo propio : metodosPropios()) {
            Optional<Metodo> deObject = object.metodo(propio.clave());
            if (deObject.isPresent() && !deObject.get().esEstatico()
                    && !propio.mismaSignaturaQue(deObject.get())) {
                throw new ExcepcionSemantica(propio.token(), "El metodo " + propio.nombre()
                        + " de la interfaz " + nombre()
                        + " redefine con otra signatura el metodo de Object");
            }
        }
    }
}
