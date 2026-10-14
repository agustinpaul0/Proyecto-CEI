// una interfaz ofrece sus metodos declarados y heredados (§10.2)
interface Ia { int f(); }
interface Ib extends Ia { boolean g(int x); }
class Cc implements Ib {
    int f() { return 1; }
    boolean g(int x) { return true; }
}
class Uso {
    void m(Ib i) {
        var a = i.f();
        var b = i.g(a);
        i = new Cc();
    }
}
