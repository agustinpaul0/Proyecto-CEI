///[Error:=|9]
// el T conservado por extends Base no es Object (§5.1)
class Base<T> {
    T obtener() { return null; }
}
class Derivada extends Base { }
class Uso {
    void m(Derivada d, Object o) {
        o = d.obtener(); // <-
    }
}
