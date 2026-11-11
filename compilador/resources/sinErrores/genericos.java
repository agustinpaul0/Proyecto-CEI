///hola&42&7&mundo&exitosamente
class Caja<T> {
    T dato;
    Caja(T d) {
        dato = d;
    }
    T obtener() {
        return dato;
    }
    void poner(T d) {
        dato = d;
    }
}

class Numero {
    int n;
    Numero(int v) {
        n = v;
    }
}

class Init {
    static void main() {
        var c = new Caja<String>("hola");
        System.printSln(c.obtener());
        var d = new Caja<Numero>(new Numero(42));
        debugPrint(d.obtener().n);
        d.poner(new Numero(7));
        debugPrint(d.dato.n);
        c.poner("mundo");
        System.printSln(c.dato);
    }
}
