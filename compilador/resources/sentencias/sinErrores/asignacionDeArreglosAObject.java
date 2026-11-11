// todo arreglo conforma con Object (§9.4)
class Aa {
    void m(Object o, int[] v, String[][] s) {
        o = v;
        o = s;
        o = new int[2];
    }
}
