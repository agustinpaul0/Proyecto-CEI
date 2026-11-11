// new con tamanios int y n dimensiones (§11.8)
interface Ix { }
class Caja<T> { }
class Aa {
    void m(int n) {
        var a = new int[3];
        var b = new boolean[n][n + 1];
        var c = new char[1][2][3];
        var d = new Aa[n];
        var e = new String[2];
        var f = new Ix[5];
        var g = new Caja<String>[10];
        var h = new Object[1];
        a = new int[a.length];
        c = new char[b.length][a[0]][n];
    }
}
