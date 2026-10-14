///[Error:=|6]
// el tipo completo de la creacion no conforma con el destino
class Caja<T> { }
class Uso {
    void m(Caja<Object> c) {
        c = new Caja<String>(); // <-
    }
}
