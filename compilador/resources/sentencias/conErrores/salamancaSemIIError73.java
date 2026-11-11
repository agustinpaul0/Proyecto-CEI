///[Error:.|5]
// CN-SII-122: no se permiten invocar metodos sobre un receptor de tipo parametrico T (S10.2)
class InvocacionT<T> {
    void m1(T elem) {
        elem.toString();
    }
}
