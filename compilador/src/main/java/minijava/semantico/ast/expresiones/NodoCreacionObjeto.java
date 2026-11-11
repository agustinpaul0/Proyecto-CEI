package minijava.semantico.ast.expresiones;

import minijava.generacion.Etiquetas;
import minijava.generacion.Generador;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.entidades.Constructor;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoReferencia;

import java.util.List;

// `new Clase<Argumento>(e1, ..., en)`. Documento §10.3 y §11.7: un tipo válido que
// nombra una clase, y el constructor de aridad n de esa clase, sin buscar en los
// ancestros. El resultado es el tipo completo.
public final class NodoCreacionObjeto extends NodoExpresion {

    private final Token nuevo;
    private final TipoReferencia tipo;
    private final List<NodoExpresion> argumentos;
    private Clase clase;
    private Constructor constructor;

    public NodoCreacionObjeto(Token nuevo, TipoReferencia tipo, List<NodoExpresion> argumentos) {
        this.nuevo = nuevo;
        this.tipo = tipo;
        this.argumentos = List.copyOf(argumentos);
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        tipo.verificarEn(contexto.clase(), contexto.esEstatico());
        ClaseOInterfaz declarado = tipo.declaracion();
        if (!(declarado instanceof Clase claseCreada)) {
            throw new ExcepcionSemantica(nuevo, tipo.nombreDelTipo() + " es una interfaz y no se instancia");
        }
        clase = claseCreada;
        constructor = clase.constructor(argumentos.size()).orElseThrow(() ->
                new ExcepcionSemantica(nuevo, clase.nombre() + " no tiene un constructor con "
                        + argumentos.size() + " parametros"));
        Argumentos.chequear(argumentos, constructor.parametros(),
                ClaseOInterfaz.sustitucionHacia(tipo, clase), nuevo, contexto);
        return tipo;
    }

    // Apunte §11, p. 21: alojar el CIR, cargarle la VT, y llamar al constructor con
    // la referencia como `this` debajo de los argumentos y como resultado debajo de
    // todo.
    @Override
    public void generar(Generador g) {
        g.gen("RMEM 1");
        g.gen("PUSH " + clase.tamanioDelCIR());
        g.gen("PUSH " + Etiquetas.MALLOC);
        g.gen("CALL");
        g.gen("DUP");
        g.gen("PUSH " + Etiquetas.tablaVirtual(clase.nombre()));
        g.gen("STOREREF 0");
        g.gen("DUP");
        for (NodoExpresion argumento : argumentos) {
            argumento.generar(g);
            g.gen("SWAP");
        }
        g.gen("PUSH " + constructor.etiqueta());
        g.gen("CALL");
    }

    @Override
    public Token token() {
        return nuevo;
    }

    @Override
    public boolean esLlamada() {
        return true;
    }

    @Override
    public String toString() {
        return "(new " + tipo.nombre() + Argumentos.comoTexto(argumentos) + ")";
    }
}
