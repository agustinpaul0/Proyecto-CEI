///[Error:|||5]
// || exige boolean en los dos lados (§11.2)
class Aa {
    void m(int i, boolean r) {
        r = r || i; // <-
    }
}
