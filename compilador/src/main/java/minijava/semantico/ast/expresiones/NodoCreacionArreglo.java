package minijava.semantico.ast.expresiones;

import minijava.generacion.RutinasDeSoporte;
import minijava.generacion.Generador;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoPrimitivo;

import java.util.List;

// `new TipoBase[e1]...[en]`. Documento §11.8: el tipo base es válido en el
// contexto, cada tamaño es int y el resultado tiene n dimensiones. No crea
// objetos ni es una llamada a un constructor.
public final class NodoCreacionArreglo extends NodoExpresion {

    private final Token nuevo;
    private final Tipo base;
    private final List<NodoExpresion> tamanios;

    public NodoCreacionArreglo(Token nuevo, Tipo base, List<NodoExpresion> tamanios) {
        this.nuevo = nuevo;
        this.base = base;
        this.tamanios = List.copyOf(tamanios);
    }

    @Override
    public Tipo chequear(Contexto contexto) throws ExcepcionSemantica {
        base.verificarEn(contexto.clase(), contexto.esEstatico());
        for (NodoExpresion tamanio : tamanios) {
            Tipo tipo = tamanio.chequear(contexto);
            if (tipo != TipoPrimitivo.INT) {
                throw new ExcepcionSemantica(tamanio.token(), "El tamanio del arreglo es de tipo "
                        + tipo.nombre() + " y tiene que ser int");
            }
        }
        return TipoArreglo.de(base, tamanios.size());
    }

    // Apunte §12. Con una dimensión, la rutina crea el arreglo. Con más, los tamaños
    // van a un arreglo de enteros que recorre la rutina recursiva desde el nivel 0.
    @Override
    public void generar(Generador g) {
        g.gen("RMEM 1");
        if (tamanios.size() == 1) {
            tamanios.getFirst().generar(g);
            g.gen("PUSH " + RutinasDeSoporte.NUEVO_ARREGLO);
            g.gen("CALL");
            return;
        }
        g.gen("RMEM 1");
        g.gen("PUSH " + tamanios.size());
        g.gen("PUSH " + RutinasDeSoporte.NUEVO_ARREGLO);
        g.gen("CALL");
        for (int i = 0; i < tamanios.size(); i++) {
            g.gen("DUP");
            tamanios.get(i).generar(g);
            g.gen("STOREREF " + (i + 2));
        }
        g.gen("PUSH 0");
        g.gen("PUSH " + RutinasDeSoporte.NUEVO_ARREGLO_MULTI);
        g.gen("CALL");
    }

    @Override
    public Token token() {
        return nuevo;
    }

    @Override
    public String toString() {
        return "(new " + base.nombre() + "[]".repeat(tamanios.size()) + Argumentos.comoTexto(tamanios) + ")";
    }
}
