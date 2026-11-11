///1&2&3&2&3&30&20&exitosamente
class Aa {
    int quien() {
        return 1;
    }
    int doble() {
        return quien() * 10;
    }
}

class Bb extends Aa {
    int quien() {
        return 2;
    }
}

class Cc extends Bb {
    int quien() {
        return 3;
    }
}

class Init {
    static int preguntar(Aa x) {
        return x.quien();
    }
    static void main() {
        debugPrint(preguntar(new Aa()));
        debugPrint(preguntar(new Bb()));
        debugPrint(preguntar(new Cc()));
        var b = new Bb();
        debugPrint(b.quien());
        var c = new Cc();
        debugPrint(c.quien());
        debugPrint(c.doble());
        debugPrint(b.doble());
    }
}
