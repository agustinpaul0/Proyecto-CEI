package minijava.semantico.ast.expresiones;

import minijava.generacion.Ubicacion;
import minijava.generacion.Generador;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Atributo;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

import java.util.Optional;

// Un nombre sin receptor. Documento §10.1: se busca una local visible, después
// un parámetro, y por último un atributo de la clase, incluidos los heredados.
public final class NodoVariable extends NodoExpresion {

    private final Token nombre;
    private Ubicacion ubicacion;

    public NodoVariable(Token nombre) {
        this.nombre = nombre;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Optional<Tipo> localOParametro = contexto.localOParametro(nombre.lexema());
        if (localOParametro.isPresent()) {
            ubicacion = contexto.ubicacionDeLocalOParametro(nombre.lexema()).orElseThrow();
            return localOParametro.get();
        }
        Optional<Atributo> atributo = contexto.clase().atributo(nombre.lexema());
        if (atributo.isEmpty()) {
            throw new ExcepcionSemantica(nombre, "La variable " + nombre.lexema() + " no esta declarada");
        }
        if (contexto.esEstatico()) {
            throw new ExcepcionSemantica(nombre, "El atributo " + nombre.lexema()
                    + " no se puede usar sin receptor en un metodo estatico");
        }
        ubicacion = new Ubicacion.EnThis(contexto.clase().offsetDeAtributo(nombre.lexema()));
        return atributo.get().tipo();
    }

    @Override
    public void generar(Generador g) {
        ubicacion.cargar(g);
    }

    @Override
    public void generarAlmacenamiento(Generador g) {
        ubicacion.almacenar(g);
    }

    @Override
    public Token token() {
        return nombre;
    }

    @Override
    public boolean esAsignable() {
        return true;
    }

    @Override
    public String toString() {
        return nombre.lexema();
    }
}
