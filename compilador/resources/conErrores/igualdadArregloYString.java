///[Error:==|5]
// un arreglo conforma con Object y nada mas (§9.4)
class Aa {
    void m(int[] a, String s, boolean r) {
        r = a == s; // <-
    }
}
