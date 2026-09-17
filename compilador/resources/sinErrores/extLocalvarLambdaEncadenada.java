///[SinErrores]
// var con cuerpo de lambda que encadena llamadas, cortado en dos lineas

class Foo {

    void probar() {
        var f = x -> x
                .aplicar()
                .resultado();
    }
}