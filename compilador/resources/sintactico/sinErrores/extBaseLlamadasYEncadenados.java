///[SinErrores]
// Llamadas estaticas, encadenadas y construccion con argumentos

class Calculadora {
    int sumar(int a, int b) {
        return a + b;
    }

    int probar(int x) {
        var r = Otra.sumar(1, 2);
        this.sumar(r, x);
        return Math.max(r, x);
    }
}

class Otra {
    static int sumar(int a, int b) {
        return a + b;
    }
}