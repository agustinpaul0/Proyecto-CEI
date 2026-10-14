// un arreglo y una interfaz ofrecen el toString de Object (§11.8, duda 5)
interface Ix { }
class Aa {
    void m(int[] v, Ix i, String s) {
        s = v.toString();
        s = i.toString();
        s = new int[3].toString();
    }
}
