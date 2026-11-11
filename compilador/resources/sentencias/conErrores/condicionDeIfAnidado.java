///[Error:x|8]
// la condicion del if de adentro
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if (true) if (x) ; // <-
    }
}
