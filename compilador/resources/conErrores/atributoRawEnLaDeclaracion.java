///[Error:Caja|5]
// fuera de la herencia, un generico sin argumento es error (§5.2)
class Caja<T> { }
class Uso {
    Caja dato; // <-
}
