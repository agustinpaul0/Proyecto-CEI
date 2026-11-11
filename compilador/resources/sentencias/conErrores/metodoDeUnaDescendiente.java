///[Error:f|5]
// un metodo de una hija no es visible en la madre
class Aa {
    void m() {
        f(); // <-
    }
}
class Bb extends Aa { void f() { } }
