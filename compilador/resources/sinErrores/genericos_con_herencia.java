///abc&abc&5&exitosamente
class Base<T> {
    T dato;
    T obtener() {
        return dato;
    }
}

class Intermedia<U> extends Base<U> {
    void guardar(U u) {
        dato = u;
    }
}

class Final extends Intermedia<String> {
}

interface Contenedor<T> {
    T sacar();
}

class Cinco implements Contenedor<Integer5> {
    Integer5 sacar() {
        return new Integer5();
    }
}

class Integer5 {
    int valor() {
        return 5;
    }
}

class Init {
    static void main() {
        var f = new Final();
        f.guardar("abc");
        System.printSln(f.obtener());
        System.printSln(f.dato);
        var c = new Cinco();
        debugPrint(c.sacar().valor());
    }
}
