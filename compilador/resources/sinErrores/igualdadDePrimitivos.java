// == y != entre primitivos del mismo tipo (§11.2)
class Aa {
    void m(int i, char c, boolean b, boolean r) {
        r = i == i;
        r = c == 'a';
        r = b != true;
        r = 'x' != c;
    }
}
