///[Error:zz|6]
// la rama else se chequea aunque la condicion sea true (§12.4)
class Aa {
    void m() {
        if (true) ;
        else zz = 1; // <-
    }
}
