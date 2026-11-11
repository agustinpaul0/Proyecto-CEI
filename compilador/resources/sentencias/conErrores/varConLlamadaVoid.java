///[Error:x|6]
// una llamada void no permite inferir un tipo (§12.3)
class Aa {
    void v() { }
    void m() {
        var x = v(); // <-
    }
}
