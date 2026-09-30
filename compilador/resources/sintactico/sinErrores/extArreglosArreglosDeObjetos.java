///[SinErrores]
// Arreglos de objetos y vacios con inicializador

class Prueba {
    void main() {
        var a = new Foo[]{new Foo(), null};
        var b = new Foo[] {};
        var c = new Foo[][]{new Foo[]{new Foo()}, {}};
    }
}

class Foo {
}