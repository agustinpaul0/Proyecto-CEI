///9&4&exitosamente
class Aa {
    int x;
    void m(int x) {
        debugPrint(x);
        debugPrint(this.x);
    }
}

class Init {
    static void main() {
        var a = new Aa();
        a.x = 4;
        a.m(9);
    }
}
