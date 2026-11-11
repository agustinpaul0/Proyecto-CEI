///[Error:+|5]
// el error del inicializador se reporta antes que el de la variable
class Aa {
    void m() {
        var x = true + 1; // <-
    }
}
