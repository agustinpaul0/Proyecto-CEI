///[Error:T|5]
// la prohibicion alcanza a los argumentos genericos (§10.4)
class Caja<T> {
    static void crear() {
        var c = new Caja<T>(); // <-
    }
}
