///[SinErrores]
// Inicializadores anidados y acceso encadenado tras la creacion

class Prueba {
    void main() {
        var a = new int[][]{{1}, {2}}[1][0];
        var b = new int[][]{{1, 2}, {3, 4}}[0][1];
    }
}