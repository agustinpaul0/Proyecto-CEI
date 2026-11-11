///1&2&3&1&2&10&20&exitosamente
class Aa {
    int a;
    void ponerA(int v) {
        a = v;
    }
}

class Bb extends Aa {
    int b;
}

class Cc extends Bb {
    int c;
    void mostrar() {
        debugPrint(a);
        debugPrint(b);
        debugPrint(c);
    }
}

class Init {
    static void main() {
        var c = new Cc();
        c.ponerA(1);
        c.b = 2;
        c.c = 3;
        c.mostrar();
        debugPrint(c.a);
        debugPrint(c.b);
        var b = new Bb();
        b.ponerA(10);
        b.b = 20;
        debugPrint(b.a);
        debugPrint(b.b);
    }
}
