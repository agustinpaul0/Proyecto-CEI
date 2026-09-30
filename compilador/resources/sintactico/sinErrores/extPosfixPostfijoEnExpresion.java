///[SinErrores]
// Postfijo dentro de expresiones aritmeticas y como argumento

class Prueba {
    void main() {
        var x = 1;
        var y = 2;
        var r = x++ + y--;
        var s = r++;
        mostrar(x--);
    }

    void mostrar(int n) {
    }
}