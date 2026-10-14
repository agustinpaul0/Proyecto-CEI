///[Error:=|8]
// desde Caja<String>, el atributo T es String
class Caja<T> {
    T dato;
}
class Uso {
    void m(Caja<String> c) {
        c.dato = 3; // <-
    }
}
