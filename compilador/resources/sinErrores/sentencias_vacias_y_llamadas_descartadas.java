///1&2&3&exitosamente
class Aa {
    int valor() {
        debugPrint(2);
        return 99;
    }
}

class Init {
    static int uno() {
        debugPrint(1);
        return 1;
    }
    static void main() {
        ;
        uno();
        new Aa().valor();
        new Aa();
        var x = 3;
        x = x;
        ;
        debugPrint(x);
    }
}
