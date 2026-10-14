///[Error:.|6]
// un metodo de instancia no se llama con el nombre de la clase (§10.2)
class Aa {
    void n() { }
    void m() {
        Aa.n(); // <-
    }
}
