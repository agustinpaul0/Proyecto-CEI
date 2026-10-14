///[Error:+|5]
// no hay concatenacion de cadenas con + (§11.2)
class Aa {
    void m(String s) {
        s = s + "x"; // <-
    }
}
