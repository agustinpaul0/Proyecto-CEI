///5&10&11&50&50&9&11&exitosamente
class Aa {
    int x;
    int y;
    void m(int p) {
        p = p + 1;
        debugPrint(p);
        x = p * 2;
        this.y = x + 1;
        debugPrint(x);
        debugPrint(y);
        var z = (x = 50);
        debugPrint(z);
        debugPrint(this.x);
    }
}

class Init {
    static void main() {
        var a = new Aa();
        a.m(4);
        a.x = 9;
        debugPrint(a.x);
        debugPrint(a.y);
    }
}
