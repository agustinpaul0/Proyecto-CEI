///[Error:==|5]
// int[] e int[][] no conforman (§9.4)
class Aa {
    void m(int[] a, int[][] b, boolean r) {
        r = a == b; // <-
    }
}
