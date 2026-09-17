///[SinErrores]
// Lambda con varios parametros separados por coma

class Sumadora {

    int probar() {
        var f = (a, b) -> a + b;
        var g = (a, b, c) -> a + b * c;
        var h = (x, y) -> x - y;
        return f.aplicar(g.aplicar(1, 2, 3), h.aplicar(5, 2));
    }
}