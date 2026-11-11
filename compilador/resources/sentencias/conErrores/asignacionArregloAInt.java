///[Error:=|5]
// un arreglo no es un int
class Aa {
    void m(int[] v, int i) {
        i = v; // <-
    }
}
