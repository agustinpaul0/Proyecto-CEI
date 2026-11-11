///3&4&6&0&24&2&5&9&24&exitosamente
class Init {
    static void main() {
        var m = new int[3][4];
        debugPrint(m.length);
        debugPrint(m[0].length);
        var i = 0;
        while (i < 3) {
            var j = 0;
            while (j < 4) {
                m[i][j] = i * j;
                j = j + 1;
            }
            i = i + 1;
        }
        debugPrint(m[2][3]);
        debugPrint(m[0][3]);
        var suma = 0;
        i = 0;
        while (i < 3) {
            var j = 0;
            while (j < 4) {
                suma = suma + m[i][j];
                j = j + 1;
            }
            i = i + 1;
        }
        debugPrint(suma + 6);
        var cubo = new int[2][3][4];
        debugPrint(cubo.length);
        m[1] = new int[5];
        debugPrint(m[1].length);
        cubo[1][2][3] = 9;
        debugPrint(cubo[1][2][3]);
        debugPrint(cubo[1].length * (cubo[0][1].length * 2));
    }
}
