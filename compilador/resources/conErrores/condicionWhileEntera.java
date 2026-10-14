///[Error:x|8]
// la condicion de un while es boolean (§12.4)
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        while (x) ; // <-
    }
}
