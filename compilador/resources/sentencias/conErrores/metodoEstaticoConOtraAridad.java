///[Error:.|6]
// (f, 1) no existe en la clase nombrada
class Aa { static void f() { } }
class Uso {
    void m() {
        Aa.f(1); // <-
    }
}
