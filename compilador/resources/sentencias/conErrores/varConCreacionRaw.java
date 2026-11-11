///[Error:Caja|9]
// var caja = new Caja() es error (§5.2)
class Caja<T> {
    T contenido;
}
class Uso {
    Caja<String> texto;
    void ejemplo() {
        var caja = new Caja(); // <-
        var otra = new Caja<String>();
    }
}
