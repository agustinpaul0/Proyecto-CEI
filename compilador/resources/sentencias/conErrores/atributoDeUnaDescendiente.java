///[Error:valor|5]
// un atributo de una hija no es visible en la madre
class Aa {
    void m() {
        valor = 1; // <-
    }
}
class Bb extends Aa { int valor; }
