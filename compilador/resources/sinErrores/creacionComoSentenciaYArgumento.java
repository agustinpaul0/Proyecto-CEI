// una creacion vale como sentencia y como argumento (§12.2)
class Aa {
    Aa(Aa otro) { }
    Aa() { }
    void m() {
        new Aa();
        new Aa(new Aa());
        new Aa(null);
    }
}
