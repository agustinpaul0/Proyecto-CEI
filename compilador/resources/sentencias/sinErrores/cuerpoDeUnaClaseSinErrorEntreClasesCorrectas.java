// varias clases correctas con cuerpos que se usan entre si
class Nodo {
    int valor;
    Nodo siguiente;
    Nodo(int v) { valor = v; }
}
class Lista {
    Nodo primero;
    int largo;
    void agregar(int v) {
        var nuevo = new Nodo(v);
        nuevo.siguiente = primero;
        primero = nuevo;
        largo = largo + 1;
    }
    int suma() {
        var total = 0;
        var actual = primero;
        while (actual != null) {
            total = total + actual.valor;
            actual = actual.siguiente;
        }
        return total;
    }
    static void main() {
        var l = new Lista();
        l.agregar(1);
        l.agregar(2);
        System.printIln(l.suma());
    }
}
