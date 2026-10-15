///[Error:=|10]
// la T que Derivada conserva de Base no es la T de Par (§5.1)
class Base<T> {
    T obtener() { return null; }
}
class Derivada extends Base { }
class Par<T> {
    T propio;
    void m(Derivada d) {
        propio = d.obtener(); // <-
    }
}
