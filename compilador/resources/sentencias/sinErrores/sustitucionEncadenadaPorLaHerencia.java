// en una cadena de herencia se componen las sustituciones (§5.1)
class Base<T> {
    T dato;
    T obtener() { return dato; }
}
class Intermedia<U> extends Base<U> {
    U deIntermedia() { return obtener(); }
}
class Final extends Intermedia<String> {
    void m(Final f) {
        var s = f.dato;
        s = f.obtener();
        s = obtener();
        s = dato;
        s = f.deIntermedia();
    }
}
