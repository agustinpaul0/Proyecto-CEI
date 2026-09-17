///[SinErrores]
// Lambda de un parametro entre parentesis

class Procesador {

    void probar() {
        var f = (x) -> x * 2;
        var g = (y) -> y;
        f = (z) -> z + 10;
        g = f;
    }
}