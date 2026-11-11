///[Error:Caja|6]
// el argumento no puede ser una generica sin su argumento (§3)
class Caja<T> { }
class Uso {
    void m() {
        var c = new Caja<Caja>(); // <-
    }
}
