///[SinErrores]
// Encadenado de acceso y miembro tras una creacion con inicializador

class Prueba {
    void main() {
        var a = new int[]{1, 2, 3}[0].length;
        var b = new int[][]{{1}, {2}}[1][0].length;
    }
}