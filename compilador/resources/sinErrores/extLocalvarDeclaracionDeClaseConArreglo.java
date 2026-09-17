///[SinErrores]
// Dimension de tipo compartida por varios declaradores

class Foo {

    void probar() {
        Objeto[] a, b = new Objeto[2];
        a = new Objeto[3];
        b[0] = a[1];
    }
}

class Objeto {
}