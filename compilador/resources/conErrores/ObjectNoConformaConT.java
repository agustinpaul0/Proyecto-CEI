///[Error:=|6]
// Object no conforma con T (§9.3)
class Caja<T> {
    T contenido;
    void guardarObjeto(Object objeto) {
        contenido = objeto; // <-
    }
}
