///[SinErrores]
// var con inicializadores que no son lambdas (literales y operaciones)

class Operaciones {

    int probar() {
        var a = 10;
        var b = 3 + 4 * 2;
        var c = -a + b;
        var d = a < b;
        var e = !d;
        return c;
    }
}