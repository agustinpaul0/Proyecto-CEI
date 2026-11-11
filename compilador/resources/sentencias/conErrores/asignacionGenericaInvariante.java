///[Error:=|6]
// Caja<String> no conforma con Caja<Object> (§9.3)
class Caja<T> { }
class Uso {
    void m(Caja<String> s, Caja<Object> o) {
        o = s; // <-
    }
}
