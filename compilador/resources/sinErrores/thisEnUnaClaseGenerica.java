// this tiene tipo Caja<T> dentro de Caja<T> (§11.3)
class Caja<T> {
    Caja<T> yo() { return this; }
    void m() {
        var c = this;
        c = yo();
    }
}
