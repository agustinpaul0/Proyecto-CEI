// CP-SII-22: llamada a metodo estatico mediante el nombre de la clase
class Util {
    static int sumar(int a, int b) {
        return a + b;
    }

    static void m1() {
        var x = Util.sumar(1, 2);
        Util.sumar(3, 4);
    }
}
