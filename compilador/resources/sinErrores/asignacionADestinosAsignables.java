// locales, parametros, atributos y componentes son asignables (§11.9)
class Aa {
    int at;
    Aa otro;
    int[] v;
    int[][] mat;
    void m(int p) {
        var l = 0;
        l = 1;
        p = 2;
        at = 3;
        this.at = 4;
        otro.at = 5;
        otro.otro.otro.at = 6;
        v[0] = 7;
        mat[1][2] = 8;
        mat[0] = v;
    }
}
