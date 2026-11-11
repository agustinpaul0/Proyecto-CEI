///[Error:=|5]
// los parentesis no hacen asignable a nada (§11.4)
class Aa {
    void m(int[] v) {
        (v.length) = 3; // <-
    }
}
