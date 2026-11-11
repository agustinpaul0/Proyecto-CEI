///soy Aa&soy Bb&soy Bb&exitosamente
class Aa {
    String toString() {
        return "soy Aa";
    }
}

class Bb extends Aa {
    String toString() {
        return "soy Bb";
    }
}

class Init {
    static void imprimir(Object o) {
        System.printSln(o.toString());
    }
    static void main() {
        imprimir(new Aa());
        imprimir(new Bb());
        var a = new Bb();
        System.printSln(a.toString());
    }
}
