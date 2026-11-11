///[Error:f|6]
// char no conforma con int (§9.1)
class Aa {
    void f(int x) { }
    void m() {
        f('a'); // <-
    }
}
