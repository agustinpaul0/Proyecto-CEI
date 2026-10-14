///[Error:new|6]
// no se instancian interfaces (§11.7)
interface Figura { }
class Uso {
    void m() {
        var f = new Figura(); // <-
    }
}
