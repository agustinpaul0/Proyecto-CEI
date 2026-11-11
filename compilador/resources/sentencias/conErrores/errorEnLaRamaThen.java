///[Error:+|5]
// la rama then se chequea
class Aa {
    void m(boolean b) {
        if (b) b = b + 1; // <-
    }
}
