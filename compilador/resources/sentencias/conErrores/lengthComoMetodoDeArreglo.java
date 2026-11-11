///[Error:.|5]
// length es un atributo y no un metodo (§11.8)
class Aa {
    void m(int[] v) {
        var n = v.length(); // <-
    }
}
