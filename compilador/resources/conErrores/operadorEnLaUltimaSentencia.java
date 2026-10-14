///[Error:*|8]
// el error esta en la ultima sentencia de un metodo largo
class Aa {
    void m(int a, boolean b) {
        a = a + 1;
        a = a - 1;
        b = !b;
        a = a * b; // <-
    }
}
