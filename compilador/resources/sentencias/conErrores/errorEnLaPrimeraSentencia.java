///[Error:zz|5]
// el error esta en la primera sentencia
class Aa {
    void m(int a) {
        zz = 1; // <-
        a = 2;
        a = 3;
    }
}
