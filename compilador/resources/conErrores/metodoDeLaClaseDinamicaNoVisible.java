///[Error:.|10]
// la seleccion usa el tipo estatico: Aa no tiene f aunque Bb si (§11.6)
class Aa { }
class Bb extends Aa { void f() { } }
class Uso {
    void m() {
        var b = new Bb();
    }
    void n(Aa a) {
        a.f(); // <-
    }
}
