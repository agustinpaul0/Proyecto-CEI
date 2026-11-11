///3&3&30&exitosamente
class Aa {
    int x;
    void ponerX(int v) {
        x = v;
    }
    int verX() {
        return x;
    }
}

class Bb extends Aa {
    int y;
    int ambos() {
        return (y * 10) + verX();
    }
}

class Init {
    static void main() {
        var b = new Bb();
        b.ponerX(3);
        debugPrint(b.verX());
        debugPrint(b.x);
        b.y = 2;
        debugPrint(b.ambos() + 7);
    }
}
