///[Error:+|5]
// + no admite referencias
class Aa {
    void m(Aa a, int i) {
        i = a + a; // <-
    }
}
