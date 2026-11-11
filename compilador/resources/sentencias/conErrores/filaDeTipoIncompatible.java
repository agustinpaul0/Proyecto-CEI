///[Error:=|5]
// la fila de un int[][] es int[], no int
class Aa {
    void m(int[][] m) {
        m[0] = 3; // <-
    }
}
