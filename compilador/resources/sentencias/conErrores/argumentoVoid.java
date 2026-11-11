///[Error:f|7]
// una llamada void no es un argumento (§9.5)
class Aa {
    void v() { }
    void f(int x) { }
    void m() {
        f(v()); // <-
    }
}
