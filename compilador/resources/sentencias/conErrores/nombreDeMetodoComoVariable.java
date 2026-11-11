///[Error:m|6]
// un metodo no es una variable
class Aa {
    int m() { return 1; }
    void n() {
        var x = m; // <-
    }
}
