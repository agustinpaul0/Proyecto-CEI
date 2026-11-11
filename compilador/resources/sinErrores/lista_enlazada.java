///3&2&1&6&exitosamente
class Nodo {
    int valor;
    Nodo siguiente;
    Nodo(int v, Nodo s) {
        valor = v;
        siguiente = s;
    }
}

class Init {
    static void main() {
        var lista = new Nodo(1, null);
        lista = new Nodo(2, lista);
        lista = new Nodo(3, lista);
        var actual = lista;
        var suma = 0;
        while (actual != null) {
            debugPrint(actual.valor);
            suma = suma + actual.valor;
            actual = actual.siguiente;
        }
        debugPrint(suma);
    }
}
