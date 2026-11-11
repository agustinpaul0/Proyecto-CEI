///[Error:.|6]
// un T no instanciado no garantiza los metodos de Object (§10.2)
class Caja<T> {
    T contenido;
    void m() {
        var s = contenido.toString(); // <-
    }
}
