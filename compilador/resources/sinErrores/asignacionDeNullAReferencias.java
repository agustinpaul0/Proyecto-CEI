// null conforma con clases, interfaces, arreglos y T (§9.5)
interface Ix { }
class Caja<T> {
    T dato;
    void m(Caja<String> c, Ix i, int[] v, String s) {
        c = null;
        i = null;
        v = null;
        s = null;
        dato = null;
    }
}
