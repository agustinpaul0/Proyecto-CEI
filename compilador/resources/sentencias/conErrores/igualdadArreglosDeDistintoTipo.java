///[Error:==|5]
// int[] y boolean[] no conforman (§9.4)
class Aa {
    void m(int[] a, boolean[] b, boolean r) {
        r = a == b; // <-
    }
}
