///[Error:<|5]
// el tamanio x < y es boolean; el error va en el operador
class Aa {
    void m(int x, int y) {
        var v = new int[x < y]; // <-
    }
}
