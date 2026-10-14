// new Caja<String>() tiene tipo Caja<String> (§11.7)
class Caja<T> {
    T dato;
    Caja(T inicial) { dato = inicial; }
}
class Uso {
    void m(Caja<String> c) {
        c = new Caja<String>("s");
        var d = new Caja<Uso>(this);
        d = new Caja<Uso>(null);
    }
}
