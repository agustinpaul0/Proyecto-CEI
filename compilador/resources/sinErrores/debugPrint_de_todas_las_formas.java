///1&2&3&4&exitosamente
class Aa {
    void m() {
        debugPrint(2);
    }
}

class Init {
    static void main() {
        debugPrint(1);
        new Aa().m();
        Object.debugPrint(3);
        var a = new Aa();
        a.debugPrint(4);
    }
}
