///[Error:=|7]
// Cajon implementa Contenedor<String> y no Contenedor<Object> (§9.3)
interface Contenedor<T> { }
class Cajon implements Contenedor<String> { }
class Uso {
    void m(Contenedor<Object> c) {
        c = new Cajon(); // <-
    }
}
