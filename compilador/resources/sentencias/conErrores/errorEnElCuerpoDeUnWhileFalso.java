///[Error:zz|6]
// el cuerpo se chequea aunque no se ejecute (§12.4)
class Aa {
    void m() {
        while (false) {
            zz = 1; // <-
        }
    }
}
