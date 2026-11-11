///[Error:x|5]
// null no determina un tipo (§12.3)
class Aa {
    void m() {
        var x = null; // <-
    }
}
