///[Error:.|7]
// por una interfaz solo se ven sus metodos (§10.2)
interface Ix { }
class Aa implements Ix { void f() { } }
class Uso {
    void m(Ix i) {
        i.f(); // <-
    }
}
