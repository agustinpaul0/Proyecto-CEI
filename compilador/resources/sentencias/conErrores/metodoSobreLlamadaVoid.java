///[Error:.|6]
// void no tiene metodos (§10.1)
class Aa {
    void v() { }
    void m() {
        v().toString(); // <-
    }
}
