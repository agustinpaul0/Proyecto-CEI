// + y - sobre int, ! sobre boolean (§11.2)
class Aa {
    void m(int i, boolean b) {
        i = -i;
        i = +i;
        i = -(i + 1);
        b = !b;
        b = !(i < 3);
    }
}
