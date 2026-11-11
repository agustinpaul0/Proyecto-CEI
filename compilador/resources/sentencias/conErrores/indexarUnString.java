///[Error:[|5]
// String no es un arreglo
class Aa {
    void m(String s) {
        var x = s[0]; // <-
    }
}
