///[Error:&&|5]
// && exige boolean (§11.2)
class Aa {
    void m(int i, boolean r) {
        r = i && i; // <-
    }
}
