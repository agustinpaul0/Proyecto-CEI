///[Error:Nada|5]
// el tipo base de un arreglo tiene que existir
class Aa {
    void m() {
        var v = new Nada[3]; // <-
    }
}
