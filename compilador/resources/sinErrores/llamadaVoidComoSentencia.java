// una llamada void vale como sentencia (§12.2)
class Aa {
    void v() { }
    static void s() { }
    void m() {
        v();
        this.v();
        s();
        Aa.s();
    }
}
