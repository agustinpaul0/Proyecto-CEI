package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Tipo;

// Una expresión del AST. `chequear` resuelve los nombres y devuelve el tipo
// (Clase 10, slide 27). La información estructural —si es un destino asignable,
// si es una llamada— la calcula cada nodo aparte: es la Alternativa 1 de la
// Clase 11 (slides 29-32), la más simple.
public abstract class NodoExpresion {

    public abstract Tipo chequear(Contexto contexto) throws ExcepcionSemantica;

    // El token donde javac ubica un error de la expresión entera, por ejemplo una
    // condición que no es boolean: el operador de una binaria, el `[` de un
    // acceso, el `.` de un acceso a atributo, el `(` de los argumentos de una
    // llamada, el nombre de una variable.
    public abstract Token token();

    public boolean esAsignable() {
        return false;
    }

    public boolean esLlamada() {
        return false;
    }

    public boolean esAsignacion() {
        return false;
    }

    // Documento §11.4: un encadenado sobre un primario parentizado no es asignable
    // ni cuenta como llamada.
    public boolean empiezaConParentesis() {
        return false;
    }
}
