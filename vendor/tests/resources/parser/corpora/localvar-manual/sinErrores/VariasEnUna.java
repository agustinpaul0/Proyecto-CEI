///[SinErrores]
// Varias variables en una sentencia, unas inicializadas y otras no

class VariasEnUna {

    int probar() {
        int x, y, z = 10;
        int a = 1, b, c = a + 2, d;
        return x + y + z + a + b + c + d;
    }
}