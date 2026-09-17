///[SinErrores]
// Lambda sin parametros

class Fabrica {

    int probar() {
        var f = () -> 42;
        var g = () -> 0;
        g = f;
        return g.aplicar();
    }
}