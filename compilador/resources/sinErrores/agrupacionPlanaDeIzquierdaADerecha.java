// los binarios van en un nivel y asocian a izquierda (§11, duda 1)
class Aa {
    void m(int a, int b, boolean r) {
        var x = a - b - a;
        r = a < b == true;
        r = a == b == false;
        var y = 1 + 2 * 3;
    }
}
