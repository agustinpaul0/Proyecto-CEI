///5&1&6&exitosamente
class Aa {
    int x;
    void m() {
        var x = 5;
        debugPrint(x);
        debugPrint(this.x);
        this.x = x + 1;
        debugPrint(this.x);
    }
}

class Init {
    static void main() {
        var a = new Aa();
        a.x = 1;
        a.m();
    }
}
