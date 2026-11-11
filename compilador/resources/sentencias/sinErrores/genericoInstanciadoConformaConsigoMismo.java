// Caja<String> conforma con Caja<String> y con Object
class Caja<T> { }
class Uso {
    void m(Caja<String> a, Caja<String> b, Object o, boolean r) {
        a = b;
        o = a;
        r = a == b;
        r = a == null;
    }
}
