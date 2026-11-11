///[Error:=|9]
// el lado izquierdo no es asignable (§11.9, §11.4)
class Aa {
    int x;
    int[] v;
    Aa crear() { return this; }
    static Aa fabrica() { return null; }
    void m() {
        new Aa() = null; // <-
    }
}
