///[Error:'a'|5]
// el indice es int; el error va en el indice, como en javac (§11.8)
class Aa {
    void m(int[] v) {
        var x = v['a']; // <-
    }
}
