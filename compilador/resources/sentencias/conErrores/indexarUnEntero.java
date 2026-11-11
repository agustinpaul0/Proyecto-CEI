///[Error:[|5]
// solo se indexa un arreglo (§11.8)
class Aa {
    void m(int n) {
        var x = n[0]; // <-
    }
}
