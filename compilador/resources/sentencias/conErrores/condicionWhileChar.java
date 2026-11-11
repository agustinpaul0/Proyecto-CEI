///[Error:'a'|8]
// char no es boolean
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        while ('a') ; // <-
    }
}
