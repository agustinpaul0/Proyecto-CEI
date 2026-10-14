///[Error:&&|5]
// una binaria chequea sus dos operandos y despues el operador
class Aa {
    void m(int i, boolean r) {
        r = (i && i) || (r + r); // <-
    }
}
