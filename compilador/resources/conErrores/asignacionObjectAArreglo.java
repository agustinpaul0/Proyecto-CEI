///[Error:=|5]
// Object no conforma con un arreglo
class Aa {
    void m(int[] v, Object o) {
        v = o; // <-
    }
}
