///[Error:T|5]
// y a los tipos base de arreglos genericos (§10.4)
class Caja<T> {
    static void crear() {
        var c = new Caja<T>[2]; // <-
    }
}
