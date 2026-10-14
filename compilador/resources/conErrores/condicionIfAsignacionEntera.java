///[Error:=|8]
// una asignacion int no es condicion (§12.4)
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        if (x = 1) ; // <-
    }
}
