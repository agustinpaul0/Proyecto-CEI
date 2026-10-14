///[Error:=|5]
// T[] no conforma con Object[] (§9.4)
class Caja<T> {
    void m(T[] ts, Object[] os) {
        os = ts; // <-
    }
}
