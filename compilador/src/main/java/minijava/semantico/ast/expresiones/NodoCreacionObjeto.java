package minijava.semantico.ast.expresiones;

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

    public NodoCreacionObjeto(Token nuevo, TipoReferencia tipo, List<NodoExpresion> argumentos) {
        this.nuevo = nuevo;
        this.tipo = tipo;
        this.argumentos = List.copyOf(argumentos);
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        tipo.verificarEn(contexto.clase(), contexto.esEstatico());
        ClaseOInterfaz declarado = tipo.declaracion();
        if (!(declarado instanceof Clase clase)) {
            throw new ExcepcionSemantica(nuevo, tipo.nombreDelTipo() + " es una interfaz y no se instancia");
        }
        Constructor constructor = clase.constructor(argumentos.size()).orElseThrow(() ->
                new ExcepcionSemantica(nuevo, clase.nombre() + " no tiene un constructor con "
                        + argumentos.size() + " parametros"));
        Argumentos.chequear(argumentos, constructor.parametros(),
                ClaseOInterfaz.sustitucionHacia(tipo, clase), nuevo, contexto);
        return tipo;
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
