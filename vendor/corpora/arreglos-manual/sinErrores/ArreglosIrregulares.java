///[SinErrores]
// Arreglos irregulares (anidados) con inicializadores

class Prueba {
    void main() {
        var a = new int[][]{{1, 2}, {3, 4}};
        var b = new int[][][]{{{1}, {2}}, {{3}}};
        var c = new int[][]{new int[]{1}, new int[]{2, 3}};
        var d = new int[][]{{0, 1}, {1, 0}};
    }
}