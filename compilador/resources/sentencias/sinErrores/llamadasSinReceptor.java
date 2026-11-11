// una llamada sin receptor busca en la clase actual y sus ancestros (§10.2)
class Aa {
    int heredado() { return 1; }
}
class Bb extends Aa {
    int propio(int x) { return x; }
    void m() {
        var a = propio(heredado());
        a = propio(propio(a));
    }
}
