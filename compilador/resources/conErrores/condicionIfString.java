///[Error:"true"|8]
// String no es boolean
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if ("true") ; // <-
    }
}
