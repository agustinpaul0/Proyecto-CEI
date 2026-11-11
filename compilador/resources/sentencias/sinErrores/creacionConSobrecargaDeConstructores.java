// el constructor se elige por aridad (§10.3)
class Aa {
    Aa() { }
    Aa(int x) { }
    Aa(int x, String s) { }
}
class Uso {
    void m() {
        var a = new Aa();
        a = new Aa(1);
        a = new Aa(1, "s");
    }
}
