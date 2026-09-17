///[SinErrores]
// Reasignacion de variables y retorno de resultados de lambdas

class Flujo {

    int probar() {
        var f = x -> x + 1;
        var g = (a, b) -> a * b;
        var h = f;
        f = g.aplicar(2, 3);
        f = f + 1;
        h = f;
        return h;
    }
}