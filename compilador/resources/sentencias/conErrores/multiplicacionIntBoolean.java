///[Error:*|5]
// * exige int y recibe boolean (§11.2)
class Aa {
    void m(int i, boolean b) {
        i = i * b; // <-
    }
}
