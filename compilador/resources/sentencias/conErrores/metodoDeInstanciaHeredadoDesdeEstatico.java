///[Error:toString|5]
// toString es de instancia y no se llama sin receptor en un estatico
class Aa {
    static void m() {
        var s = toString(); // <-
    }
}
