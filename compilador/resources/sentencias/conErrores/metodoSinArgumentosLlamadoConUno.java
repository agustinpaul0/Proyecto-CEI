///[Error:f|6]
// (f, 1) no existe aunque exista (f, 0)
class Aa {
    void f() { }
    void m() {
        f(1); // <-
    }
}
