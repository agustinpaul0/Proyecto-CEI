///[Error:new|8]
// una referencia no es condicion; el error va en el new, como en javac
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if (new Aa()) ; // <-
    }
}
