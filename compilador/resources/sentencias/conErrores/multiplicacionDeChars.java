///[Error:*|5]
// * no admite char: no hay promocion numerica (§11.2)
class Aa {
    void m(char c, int i) {
        i = c * c; // <-
    }
}
