///[Error:x|5]
// la forma se controla antes que el inicializador
class Aa {
    void m(boolean c) {
        if (c) var x = zz; // <-
    }
}
