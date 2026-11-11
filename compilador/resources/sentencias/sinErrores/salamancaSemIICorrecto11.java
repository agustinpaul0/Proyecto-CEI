// CP-SII-06: igualdad == y != para primitivos, referencias y null
class Igualdad {
    Igualdad objeto;
    int[] arreglo;

    boolean m1(int a, int b, char c, char d, boolean e, boolean f) {
        var r1 = a == b;
        var r2 = c != d;
        var r3 = e == f;
        var r4 = objeto != null;
        var r5 = arreglo == null;
        return (r1 && r2) || (r3 && (r4 || r5));
    }
}
