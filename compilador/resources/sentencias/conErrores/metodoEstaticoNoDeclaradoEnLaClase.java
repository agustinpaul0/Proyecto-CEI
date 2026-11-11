///[Error:.|6]
// el metodo no esta en la clase nombrada
class Aa { }
class Uso {
    void m() {
        Aa.f(); // <-
    }
}
