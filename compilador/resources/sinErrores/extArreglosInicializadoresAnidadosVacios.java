///[SinErrores]
// Inicializadores anidados incluyendo vacios internos

class Prueba {
    void main() {
        var a = new int[][]{{}, {1}};
        var b = new int[][][]{{{}}, {{2, 3}}};
        var c = new boolean[][]{{}, {true, false}, {}};
    }
}