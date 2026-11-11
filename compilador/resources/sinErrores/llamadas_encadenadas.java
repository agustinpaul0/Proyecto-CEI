///7&8&9&3&exitosamente
class Cc {
    int valor;
}

class Bb {
    Cc c;
    Cc obtenerC() {
        return c;
    }
}

class Aa {
    Bb b;
    Bb obtenerB() {
        return b;
    }
}

class Init {
    static void main() {
        var a = new Aa();
        a.b = new Bb();
        a.b.c = new Cc();
        a.b.c.valor = 7;
        debugPrint(a.obtenerB().obtenerC().valor);
        a.obtenerB().c.valor = 8;
        debugPrint(a.b.c.valor);
        a.obtenerB().obtenerC().valor = 9;
        debugPrint(a.b.obtenerC().valor);
        debugPrint((a.b.c.valor - 6));
    }
}
