///[Error:f|6]
// (f, 2) no existe aunque exista (f, 1) (§10.2)
class Aa {
    void f(int x) { }
    void m() {
        f(1, 2); // <-
    }
}
