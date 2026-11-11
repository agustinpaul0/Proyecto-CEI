///[Error:zz|6]
// las sentencias despues de un return se chequean (§12.6)
class Aa {
    void m() {
        return;
        zz = 1; // <-
    }
}
