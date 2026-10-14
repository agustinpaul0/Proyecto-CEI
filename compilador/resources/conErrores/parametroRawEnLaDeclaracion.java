///[Error:Caja|5]
// un parametro tampoco puede ser raw (§5.2, duda 3)
class Caja<T> { }
class Uso {
    void m(Caja c) { } // <-
}
