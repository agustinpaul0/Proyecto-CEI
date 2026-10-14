///[Error:-|5]
// - unario exige int (§11.2)
class Aa {
    void m(boolean b, int i) {
        i = -b; // <-
    }
}
