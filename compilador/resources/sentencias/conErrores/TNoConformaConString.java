///[Error:=|6]
// T no conforma con String (§9.3)
class Caja<T> {
    T contenido;
    void m(String s) {
        s = contenido; // <-
    }
}
