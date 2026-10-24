///[Error:recibir|8]
// lo que hereda sin argumento conserva la T: no es Base<String> (§5.2, sin conversiones unchecked)
class Base<T> { }
class Derivada extends Base { }
class Uso {
    void recibir(Base<String> b) { }
    void m() {
        recibir(new Derivada()); // <-
    }
}
