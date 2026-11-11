///[Error:T|5]
// T[] solo dentro de la clase que declara T
class Aa {
    void m() {
        var v = new T[3]; // <-
    }
}
