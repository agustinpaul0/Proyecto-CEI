///[Error:.|5]
// un arreglo ofrece solo el toString de Object, no debugPrint (§13)
class Aa {
    void m(int[] v) {
        v.debugPrint(1); // <-
    }
}
