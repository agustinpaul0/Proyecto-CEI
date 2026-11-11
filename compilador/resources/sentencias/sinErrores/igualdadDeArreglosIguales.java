// dos arreglos del mismo tipo se comparan (§11.2)
class Aa {
    void m(int[] a, int[] b, Object o, boolean r) {
        r = a == b;
        r = a != o;
    }
}
