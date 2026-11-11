// una clase que implementa Contenedor<String> conforma con Contenedor<String> (§9.3)
interface Contenedor<T> { T sacar(); }
class Cajon implements Contenedor<String> {
    String sacar() { return "x"; }
}
class Uso {
    void m(Contenedor<String> c) {
        c = new Cajon();
        var s = c.sacar();
        s = new Cajon().sacar();
    }
}
