// con extends Base sin argumento, lo heredado conserva T (§5)
class Base<T> {
    T dato;
    T obtener() { return dato; }
}
class Derivada extends Base {
    void m(Derivada d) {
        var t = d.obtener();
        t = d.dato;
        d.dato = t;
        t = null;
    }
}
