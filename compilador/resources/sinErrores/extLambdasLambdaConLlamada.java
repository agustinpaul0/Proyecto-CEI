///[SinErrores]
// Lambdas pasadas como argumentos y combinadas con llamadas

class Ordenador {

    void probar(Operacion op) {
        op.aplicar(x -> x + 1);
        op.aplicar((a, b) -> a - b);
        var r = op.componer(x -> x * 2, y -> y + 1);
        r = op.componer((x) -> x, (a, b, c) -> a + b + c);
    }
}

class Operacion {
    void aplicar(int n) {
    }

    void componer(int f, int g) {
    }
}