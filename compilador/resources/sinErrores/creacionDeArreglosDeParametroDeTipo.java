// en una unidad de instancia de Caja<T> se crean T[] y Caja<T>[] (§11.8)
class Caja<T> {
    void m() {
        var e = new T[10];
        var c = new Caja<T>[3][4];
        e[0] = null;
    }
}
