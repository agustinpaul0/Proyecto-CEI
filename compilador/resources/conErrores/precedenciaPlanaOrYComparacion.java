///[Error:|||5]
// en un solo nivel b || x == 1 es (b || x) == 1 (§11)
class Aa {
    void m(boolean b, int x, boolean r) {
        r = b || x == 1; // <-
    }
}
