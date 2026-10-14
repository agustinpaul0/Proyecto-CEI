///[Error:x|6]
// el if de adentro tambien exige bloque
class Aa {
    void m(boolean c) {
        if (c) {
            if (c) var x = 1; // <-
        }
    }
}
