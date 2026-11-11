///[Error:p|6]
// un parametro de otro metodo no es visible
class Aa {
    void m(int p) { }
    void n() {
        p = 2; // <-
    }
}
