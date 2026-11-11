///[Error:=|7]
// una interfaz no conforma con la que la extiende
interface Ia { }
interface Ib extends Ia { }
class Uso {
    void m(Ia a, Ib b) {
        b = a; // <-
    }
}
