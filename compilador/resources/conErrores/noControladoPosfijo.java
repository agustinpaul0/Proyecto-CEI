///[Error:++|7]
// el ++ posfijo es de un logro que no se controla (duda 2)
class Aa {
    int a1;
    int a2;
    void m1() {
        a2 = a1++ + 10; // <-
    }
}
