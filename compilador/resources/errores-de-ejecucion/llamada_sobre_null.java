///Desreferenciameinto de puntero nulo
class Aa {
    Aa otro;
    void m() {
        debugPrint(1);
    }
}
class Init {
    static void main() {
        new Aa().otro.m();
    }
}
