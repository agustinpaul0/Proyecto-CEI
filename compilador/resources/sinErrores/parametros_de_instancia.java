///1&2&3&321&exitosamente
class Aa {
    int base;
    int combinar(int a, int b, int c) {
        debugPrint(a);
        debugPrint(b);
        debugPrint(c);
        return (c * 100) + (b * 10) + a + base;
    }
}

class Init {
    static void main() {
        var a = new Aa();
        debugPrint(a.combinar(1, 2, 3));
    }
}
