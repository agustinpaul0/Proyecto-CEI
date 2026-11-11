///[Error:=|6]
// Caja<Object> no conforma con Caja<String> (§9.3)
class Caja<T> { }
class Uso {
    void m(Caja<String> s, Caja<Object> o) {
        s = o; // <-
    }
}
