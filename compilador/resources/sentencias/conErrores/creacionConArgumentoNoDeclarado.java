///[Error:Nada|6]
// el argumento de tipo tiene que existir (§3)
class Caja<T> { }
class Uso {
    void m() {
        var c = new Caja<Nada>(); // <-
    }
}
