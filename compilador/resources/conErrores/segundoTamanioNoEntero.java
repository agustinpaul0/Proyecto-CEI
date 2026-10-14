///[Error:b|5]
// el segundo tamanio tambien es int (§11.8)
class Aa {
    void m(boolean b) {
        var v = new int[2][b]; // <-
    }
}
