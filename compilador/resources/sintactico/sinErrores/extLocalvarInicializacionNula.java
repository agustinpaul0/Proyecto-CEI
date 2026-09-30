///[SinErrores]
// Inicializadores null y con new mezclados en una misma sentencia

class Foo {

    void probar() {
        Objeto s = null, t = new Objeto(), u;
        u = s;
        return;
    }
}

class Objeto {
}