package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.entidades.ClaveDeMetodo;
import minijava.semantico.entidades.Interfaz;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoReferencia;

import java.util.List;
import java.util.Optional;

// `receptor.m(e1, ..., en)`. Documento §10.2: se busca (m, n) en el tipo estático
// del receptor. Por un receptor se puede elegir también un método estático.
public final class NodoLlamadaConReceptor extends NodoEncadenado {

    private final Token parentesis;
    private final List<NodoExpresion> argumentos;

    public NodoLlamadaConReceptor(NodoExpresion receptor, Token punto, Token nombre, Token parentesis,
                                  List<NodoExpresion> argumentos) {
        super(receptor, punto, nombre);
        this.parentesis = parentesis;
        this.argumentos = List.copyOf(argumentos);
    }

    @Override
    public Token token() {
        return parentesis;
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        Tipo tipoDelReceptor = receptor.chequear(contexto);
        ClaveDeMetodo clave = new ClaveDeMetodo(nombre.lexema(), argumentos.size());
        Sustitucion sustitucion = Sustitucion.identidad();
        Optional<Metodo> metodo;
        switch (tipoDelReceptor) {
            case TipoReferencia referencia -> {
                ClaseOInterfaz declarado = TablaDeSimbolos.tipo(referencia.nombreDelTipo()).orElseThrow();
                sustitucion = ClaseOInterfaz.sustitucionHacia(referencia, declarado);
                metodo = declarado.metodo(clave);
                if (declarado instanceof Interfaz) {
                    metodo = metodo.or(() -> deObject(clave));
                }
            }
            case TipoArreglo arreglo -> metodo = deObject(clave);
            default -> throw sinMiembros(tipoDelReceptor);
        }
        Metodo elegido = metodo.orElseThrow(() -> noDeclarado("El metodo " + clave, tipoDelReceptor));
        Argumentos.chequear(argumentos, elegido.parametros(), sustitucion, nombre, contexto);
        return elegido.retorno().instanciar(sustitucion);
    }

    // Una interfaz y un arreglo ofrecen los métodos de instancia de Object
    // (JLS §9.2 y documento §11.8); debugPrint es estático y queda afuera.
    private static Optional<Metodo> deObject(ClaveDeMetodo clave) {
        return TablaDeSimbolos.object().metodo(clave).filter(metodo -> !metodo.esEstatico());
    }

    @Override
    public boolean esLlamada() {
        return !empiezaConParentesis();
    }

    @Override
    public String toString() {
        return "(llamar " + receptor + "." + nombre.lexema() + Argumentos.comoTexto(argumentos) + ")";
    }
}
