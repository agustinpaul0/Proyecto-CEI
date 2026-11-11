///[Error:[|6]
// void no se indexa
class Aa {
    void v() { }
    void m() {
        var x = v()[0]; // <-
    }
}
