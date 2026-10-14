///[Error:.|5]
// un primitivo no tiene atributos (§10.1)
class Aa {
    void m(int n) {
        var x = n.x; // <-
    }
}
