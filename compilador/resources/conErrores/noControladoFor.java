///[Error:for|5]
// el for es de un logro que no se controla
class Aa {
    void m() {
        for (var i = 0; i < 3; i = i + 1) ; // <-
    }
}
