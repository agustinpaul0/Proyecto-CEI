///1&Desreferenciameinto de puntero nulo
class Aa {
    int x;
    Aa otro;
}
class Init {
    static void main() {
        var a = new Aa();
        debugPrint(1);
        debugPrint(a.otro.x);
    }
}
