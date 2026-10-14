///[Error:zz|5]
// los metodos estaticos tambien se chequean
class Aa {
    static void m() {
        zz = 1; // <-
    }
}
