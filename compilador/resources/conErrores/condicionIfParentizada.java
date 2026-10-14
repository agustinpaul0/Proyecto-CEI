///[Error:x|8]
// el parentesis conserva el tipo int; el error va adentro, como en javac
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if ((x)) ; // <-
    }
}
