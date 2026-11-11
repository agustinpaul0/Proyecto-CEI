///[Error:zz|5]
// el operando derecho se chequea antes que el operador
class Aa {
    void m(boolean r) {
        r = 1 + zz; // <-
    }
}
