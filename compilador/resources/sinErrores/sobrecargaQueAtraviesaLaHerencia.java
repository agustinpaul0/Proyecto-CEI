// la sobrecarga por aridad combina propios y heredados
class Aa { int f() { return 0; } }
class Bb extends Aa {
    boolean f(int x) { return true; }
    void m(Bb b) {
        var i = f();
        var j = b.f(1);
        i = 1;
        j = false;
    }
}
