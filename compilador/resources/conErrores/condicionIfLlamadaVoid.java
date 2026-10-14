///[Error:(|8]
// una llamada void no es condicion
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if (v()) ; // <-
    }
}
