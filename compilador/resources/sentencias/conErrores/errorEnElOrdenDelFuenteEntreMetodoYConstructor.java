///[Error:yy|5]
// las unidades se chequean en el orden del fuente, como javac
class Aa {
    void m() {
        yy = 1; // <-
    }
    Aa() {
        zz = 2;
    }
}
