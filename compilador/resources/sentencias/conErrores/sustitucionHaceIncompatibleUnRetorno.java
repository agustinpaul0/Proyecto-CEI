///[Error:=|8]
// desde Caja<Uso>, obtener() es Uso y no String
class Caja<T> {
    T obtener() { return null; }
}
class Uso {
    void m(Caja<Uso> c, String s) {
        s = c.obtener(); // <-
    }
}
