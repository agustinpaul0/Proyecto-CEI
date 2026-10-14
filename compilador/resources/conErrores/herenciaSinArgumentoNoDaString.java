///[Error:=|9]
// el T conservado no se reemplaza por un tipo concreto (§5)
class Base<T> {
    T dato;
}
class Derivada extends Base { }
class Uso {
    void m(Derivada d) {
        d.dato = "s"; // <-
    }
}
