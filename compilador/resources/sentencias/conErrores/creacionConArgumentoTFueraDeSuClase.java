///[Error:T|6]
// una T vale solo en la clase que la declara (§3)
class Caja<T> { }
class Uso {
    void m() {
        var c = new Caja<T>(); // <-
    }
}
