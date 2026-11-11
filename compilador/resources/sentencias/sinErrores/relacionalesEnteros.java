// los relacionales sobre int dan boolean (§11.2)
class Aa {
    void m(int a, int b, boolean r) {
        r = a < b;
        r = a > b;
        r = a <= b;
        r = a >= b;
    }
}
