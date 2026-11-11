///[Error:=|8]
// desde Caja<String>, T[] es String[]
class Caja<T> {
    T[] varios;
}
class Uso {
    void m(Caja<String> c) {
        c.varios = new int[3]; // <-
    }
}
