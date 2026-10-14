///[Error:x|8]
// la condicion de un if es boolean (§12.4)
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if (x) ; // <-
    }
}
