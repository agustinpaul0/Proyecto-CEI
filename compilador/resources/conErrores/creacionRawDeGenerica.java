///[Error:Caja|6]
// una clase generica no se instancia sin argumento (§5.2)
class Caja<T> { }
class Uso {
    void m() {
        var caja = new Caja(); // <-
    }
}
