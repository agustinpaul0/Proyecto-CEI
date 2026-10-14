///[Error:zz|5]
// la expresion se chequea antes que su forma (§11, 1.er parrafo)
class Aa {
    void m() {
        true + zz; // <-
    }
}
