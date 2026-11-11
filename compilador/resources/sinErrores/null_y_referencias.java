///true&false&true&5&true&exitosamente
class Nodo {
    Nodo siguiente;
    int valor;
}

class Init {
    static void main() {
        var n = new Nodo();
        System.printBln(n.siguiente == null);
        n.siguiente = new Nodo();
        System.printBln(n.siguiente == null);
        var m = n.siguiente;
        System.printBln(m == n.siguiente);
        n.siguiente.valor = 5;
        debugPrint(m.valor);
        System.printBln(n != m);
    }
}
