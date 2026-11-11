///[Error:x|5]
// el inicializador no ve la variable que declara (§12.3)
class Aa {
    void m() {
        var x = x; // <-
    }
}
