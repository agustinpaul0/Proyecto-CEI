///[Error:x|5]
// tampoco de un while (§12.3)
class Aa {
    void m(boolean c) {
        while (c) var x = 1; // <-
    }
}
