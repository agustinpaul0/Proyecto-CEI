///[Error:zz|5]
// el lado izquierdo se chequea antes que el derecho
class Aa {
    void m() {
        zz = yy; // <-
    }
}
