// una llamada que produce valor vale como sentencia; el valor se descarta (§12.2)
class Aa {
    int n() { return 1; }
    Aa yo() { return this; }
    void m() {
        n();
        yo().yo().n();
        new Aa();
        new Aa().n();
    }
}
