///[Error:contenido|6]
// T no conforma con Object: no tiene cotas (§9.3)
class Caja<T> {
    T contenido;
    Object comoObjeto() {
        return contenido; // <-
    }
}
