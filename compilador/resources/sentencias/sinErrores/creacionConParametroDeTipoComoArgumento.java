// dentro de Caja<T>, new Caja<T>() es valido
class Caja<T> {
    Caja<T> copia() { return new Caja<T>(); }
}
