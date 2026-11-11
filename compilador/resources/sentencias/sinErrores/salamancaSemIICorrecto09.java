// CP-SII-04: operadores relacionales < <= > >=
class Relacional {
    boolean comparar(int a, int b) {
        var x = a < b;
        var y = a <= b;
        var z = a > b;
        var w = a >= b;
        return (x && y) || (z && w);
    }
}
