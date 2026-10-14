///[Error:==|6]
// void no se compara (§11.2)
class Aa {
    void v() { }
    void m(boolean r) {
        r = v() == v(); // <-
    }
}
