///[Error:.|5]
// un primitivo no tiene metodos (§10.1)
class Aa {
    void m(int x) {
        x.f(); // <-
    }
}
