///[Error:<=|5]
// <= exige int (§11.2)
class Aa {
    void m(boolean a, boolean r) {
        r = a <= a; // <-
    }
}
