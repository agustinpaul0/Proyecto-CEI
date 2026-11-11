// CP-SII-02: operadores unarios +, - y !
class Unarios {
    int negar(int x) {
        var a = -5;
        var b = +x;
        var c = -a;
        return b + c;
    }

    boolean invertir(int x) {
        return !(x > 0);
    }
}
