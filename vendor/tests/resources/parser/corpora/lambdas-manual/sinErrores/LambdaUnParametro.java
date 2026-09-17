///[SinErrores]
// Lambda de un solo parametro sin parentesis

class Calculadora {
    int doble(int n) {
        return n * 2;
    }

    void probar(Aplicador op) {
        var f = x -> x + 1;
        var g = x -> x * 2;
        f = g;
        op.aplicar(f);
    }
}

class Aplicador {
    void aplicar(int n) {
    }
}