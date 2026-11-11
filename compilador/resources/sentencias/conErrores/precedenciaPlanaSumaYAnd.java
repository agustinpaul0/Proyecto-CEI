///[Error:&&|6]
// en un solo nivel x + 1 > 0 && b vale, y x > 0 && x < 1 es ((x > 0) && x) < 1 y falla en el && (§11)
class Aa {
    void m(boolean b, int x, boolean r) {
        r = x + 1 > 0 && b;
        r = x > 0 && x < 1; // <-
    }
}
