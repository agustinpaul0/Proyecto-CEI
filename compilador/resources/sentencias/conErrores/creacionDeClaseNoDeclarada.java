///[Error:Nada|5]
// el tipo de un new tiene que existir (§10.3)
class Aa {
    void m() {
        var x = new Nada(); // <-
    }
}
