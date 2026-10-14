///[Error:x|5]
// una local no puede ocultar un parametro (§12.3)
class Aa {
    void m(int x) {
        var x = 1; // <-
    }
}
