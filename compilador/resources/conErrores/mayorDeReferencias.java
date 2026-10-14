///[Error:>|5]
// > no admite referencias
class Aa {
    void m(String s, boolean r) {
        r = s > s; // <-
    }
}
