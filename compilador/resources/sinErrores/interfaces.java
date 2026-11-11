///7&3&10&3&exitosamente
interface Valuable {
    int valor();
}

class Siete implements Valuable {
    int valor() {
        return 7;
    }
}

class Tres implements Valuable {
    int extra() {
        return 100;
    }
    int valor() {
        return 3;
    }
}

class Init {
    static int sumar(Valuable a, Valuable b) {
        return a.valor() + b.valor();
    }
    static void main() {
        var s = new Siete();
        var t = new Tres();
        debugPrint(s.valor());
        debugPrint(t.valor());
        debugPrint(sumar(s, t));
        debugPrint(sumar(t, t) / 2);
    }
}
