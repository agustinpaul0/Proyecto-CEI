// cada indexacion quita una dimension; length es int (§11.8)
class Aa {
    void m(int[][] mat, String[] s, int i) {
        i = mat[0][1];
        i = mat.length;
        i = mat[0].length;
        var fila = mat[i];
        fila[0] = 1;
        mat[0] = fila;
        s[i] = "x";
        var t = s[mat[0][0]];
        t = "y";
    }
}
