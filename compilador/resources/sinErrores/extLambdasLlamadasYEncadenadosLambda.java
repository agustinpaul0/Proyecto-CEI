///[SinErrores]
// Llamadas estaticas y encadenadas junto a variables var

class Cadena {

    static int inicial() {
        return 1;
    }

    int probar() {
        var a = Cadena.inicial();
        var b = a + Cadena.inicial();
        return b;
    }
}

class Otro {
    int sumar(int x, int y) {
        return x + y;
    }
}