///[Error:=|5]
// el componente de un int[] es int
class Aa {
    void m(int[] v) {
        v[0] = true; // <-
    }
}
