///[Error:+|5]
// + unario exige int (§11.2)
class Aa {
    void m(char c, int i) {
        i = +c; // <-
    }
}
