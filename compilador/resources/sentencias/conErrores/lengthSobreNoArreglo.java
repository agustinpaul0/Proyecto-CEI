///[Error:.|5]
// length es solo de los arreglos (§11.8)
class Aa {
    void m(Aa a) {
        var x = a.length; // <-
    }
}
