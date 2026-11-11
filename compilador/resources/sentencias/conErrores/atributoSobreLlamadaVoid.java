///[Error:.|7]
// void no tiene atributos
class Aa {
    int x;
    void v() { }
    void m() {
        var y = v().x; // <-
    }
}
