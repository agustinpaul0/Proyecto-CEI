///[Error:zz|5]
// el receptor se chequea antes que el metodo
class Aa {
    void m() {
        zz.f(); // <-
    }
}
