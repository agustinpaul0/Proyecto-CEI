///[Error:valor|6]
// un atributo de otra clase no es visible sin receptor
class Bb { int valor; }
class Aa {
    void m() {
        valor = 1; // <-
    }
}
