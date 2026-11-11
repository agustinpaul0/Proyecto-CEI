///[Error:+|5]
// + no admite null
class Aa {
    void m(int i) {
        i = i + null; // <-
    }
}
