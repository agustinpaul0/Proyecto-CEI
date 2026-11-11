///[Error:Caja|5]
// un retorno tampoco puede ser raw (§5.2)
class Caja<T> { }
class Uso {
    Caja m() { return null; } // <-
}
