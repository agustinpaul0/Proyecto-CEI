///[Error:=|9]
// Der<T> extends Base: la T heredada es la de Base, no la de Der (§5.1, duda 4)
class Base<T> {
    T dato;
}
class Der<T> extends Base {
    T propio;
    void m() {
        propio = dato; // <-
    }
}
