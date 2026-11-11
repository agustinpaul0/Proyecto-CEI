///[Error:Nada|5]
// la clase de una llamada estatica tiene que existir (§10.2)
class Aa {
    void m() {
        Nada.f(); // <-
    }
}
