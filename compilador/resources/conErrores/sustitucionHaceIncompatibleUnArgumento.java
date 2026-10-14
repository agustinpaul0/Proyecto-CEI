///[Error:guardar|8]
// desde Caja<String> el parametro T se chequea como String (§5.1)
class Caja<T> {
    void guardar(T valor) { }
}
class Uso {
    void m(Caja<String> c) {
        c.guardar(3); // <-
    }
}
