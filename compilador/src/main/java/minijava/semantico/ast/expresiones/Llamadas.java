package minijava.semantico.ast.expresiones;

import minijava.generacion.Generador;
import minijava.semantico.entidades.Metodo;

import java.util.List;

// Las dos secuencias de llamada del Apunte, compartidas por las tres formas de
// llamar a un método. La celda del resultado se reserva con PUSH 0 y no con el
// RMEM 1 del Apunte: RMEM sólo mueve el tope y deja lo que había en la pila, y un
// método que termina sin return (documento §12.5) devolvería basura.
final class Llamadas {

    private Llamadas() {
    }

    // Apunte §8: la celda del resultado, los argumentos en orden y el salto a una
    // dirección que se conoce al compilar.
    static void estatica(Generador g, Metodo metodo, List<NodoExpresion> argumentos) {
        if (!metodo.esVoid()) {
            g.gen("PUSH 0");
        }
        for (NodoExpresion argumento : argumentos) {
            argumento.generar(g);
        }
        g.gen("PUSH " + metodo.etiqueta());
        g.gen("CALL");
    }

    // Apunte §10, p. 19: con el receptor ya en el tope, cada SWAP lo lleva por
    // encima del último valor, hasta quedar en la celda de `this`. La dirección del
    // método sale de la VT del objeto: su desplazamiento es el mismo en toda la
    // jerarquía.
    static void dinamica(Generador g, Metodo metodo, int offsetEnVT, List<NodoExpresion> argumentos) {
        if (!metodo.esVoid()) {
            g.gen("PUSH 0");
            g.gen("SWAP");
        }
        for (NodoExpresion argumento : argumentos) {
            argumento.generar(g);
            g.gen("SWAP");
        }
        g.gen("DUP");
        g.gen("LOADREF 0");
        g.gen("LOADREF " + offsetEnVT);
        g.gen("CALL");
    }
}
