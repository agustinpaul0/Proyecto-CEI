///[Error:this|8]
// una referencia no se convierte a boolean (§12.4)
class Aa {
    void v() { }
    int n() { return 1; }
    static int e() { return 1; }
    void m(int x) {
        while (this) ; // <-
    }
}
