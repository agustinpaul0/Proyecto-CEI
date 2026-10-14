///[Error:?|5]
// el ternario es de un logro que no se controla
class Aa {
    void m(boolean b) {
        var x = b ? 1 : 2; // <-
    }
}
