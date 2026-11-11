///[Error:(|8]
// una llamada estatica int no es condicion; el error va en el (, como en javac
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if (Aa.e()) ; // <-
    }
}
