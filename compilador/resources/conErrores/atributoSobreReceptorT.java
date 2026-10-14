///[Error:.|6]
// un T no permite seleccionar atributos (§10.1)
class Caja<T> {
    T contenido;
    void m() {
        var x = contenido.dato; // <-
    }
}
