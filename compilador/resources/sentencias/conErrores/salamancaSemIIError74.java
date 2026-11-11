///[Error:.|5]
// CN-SII-123: no se permiten acceder atributos sobre un receptor de tipo parametrico T (S10.1)
class AccesoT<T> {
    void m1(T elem) {
        var v = elem.dato;
    }
}
