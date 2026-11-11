///[Error:+|5]
// en un solo nivel x < y + 1 es (x < y) + 1 (§11)
class Aa {
    void m(int x, int y) {
        var b = x < y + 1; // <-
    }
}
