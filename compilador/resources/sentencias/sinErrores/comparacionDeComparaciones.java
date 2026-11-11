// (a < b) == (c < d) compara dos boolean
class Aa {
    void m(int a, int b, boolean r) {
        r = (a < b) == (b < a);
        r = !(a == b) && (a != b);
    }
}
