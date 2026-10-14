///[Error:3|6]
// el parametro T se ve como String desde Caja<String> (§11.7)
class Caja<T> { Caja(T inicial) { } }
class Uso {
    void m() {
        var c = new Caja<String>(3); // <-
    }
}
