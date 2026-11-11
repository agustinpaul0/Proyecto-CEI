///[Error:+|5]
// + no convierte int a String (§11.2)
class Aa {
    void m(String s) {
        s = "x" + 1; // <-
    }
}
