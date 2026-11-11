///[Error:null|5]
// cada tamanio es int; el error va en el tamanio, como en javac (§11.8)
class Aa {
    void m() {
        var v = new int[null]; // <-
    }
}
