// el retorno de una llamada se usa como operando o como receptor
class Aa {
    int x;
    Aa yo() { return this; }
    int n() { return 2; }
    boolean m() {
        return yo().x + n() * yo().yo().n() > 0;
    }
}
