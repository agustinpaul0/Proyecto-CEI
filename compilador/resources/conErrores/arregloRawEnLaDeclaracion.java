///[Error:Caja|5]
// ni el tipo base de un arreglo (§5.2, duda 3)
class Caja<T> { }
class Uso {
    Caja[] datos; // <-
}
