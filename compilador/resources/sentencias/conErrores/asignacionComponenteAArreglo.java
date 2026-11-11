///[Error:=|5]
// un componente int no es un int[]
class Aa {
    void m(int[] v) {
        v = v[0]; // <-
    }
}
