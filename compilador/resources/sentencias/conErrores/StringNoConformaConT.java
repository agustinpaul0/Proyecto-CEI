///[Error:=|6]
// un valor String no conforma con un destino T (§9.3)
class Caja<T> {
    T contenido;
    void m() {
        contenido = "s"; // <-
    }
}
