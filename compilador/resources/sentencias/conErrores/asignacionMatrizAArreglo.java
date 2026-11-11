///[Error:=|5]
// int[][] no conforma con int[] (§9.4)
class Aa {
    void m(int[][] m, int[] v) {
        v = m; // <-
    }
}
