///[Error:T|5]
// T no se usa en el cuerpo de un estatico (§10.4)
class Caja<T> {
    static void crear() {
        var datos = new T[10]; // <-
    }
}
