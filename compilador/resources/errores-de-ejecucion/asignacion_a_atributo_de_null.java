///Desreferenciameinto de puntero nulo
class Aa {
    int x;
    Aa otro;
}
class Init {
    static void main() {
        var a = new Aa();
        a.otro.x = 4;
    }
}
