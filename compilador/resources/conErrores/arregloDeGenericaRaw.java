///[Error:Caja|6]
// el tipo base no puede ser una generica sin argumento (§11.8)
class Caja<T> { }
class Uso {
    void m() {
        var v = new Caja[3]; // <-
    }
}
