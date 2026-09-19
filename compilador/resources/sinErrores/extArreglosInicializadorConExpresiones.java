///[SinErrores]
// Elementos del inicializador como expresiones arbitrarias

class Prueba {
    int campo() {
        return 2;
    }

    void main() {
        var x = 2;
        var a = new int[]{x + 1, x * 3, -x};
        var b = new int[]{this.campo(), 5, 3 - 1};
    }
}