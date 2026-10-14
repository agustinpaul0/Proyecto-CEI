// null se compara con referencias, arreglos y consigo mismo (§9.5)
class Aa {
    void m(Aa a, int[] v, String s, boolean r) {
        r = a == null;
        r = null != v;
        r = s == null;
        r = null == null;
    }
}
