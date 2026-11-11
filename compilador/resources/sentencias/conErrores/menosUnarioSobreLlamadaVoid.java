///[Error:-|6]
// un resultado void no se usa con operadores (§11.2)
class Aa {
    void v() { }
    void m(int i) {
        i = -v(); // <-
    }
}
