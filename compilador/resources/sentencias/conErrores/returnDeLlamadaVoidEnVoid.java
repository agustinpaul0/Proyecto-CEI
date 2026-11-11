///[Error:(|6]
// un retorno con resultado void es incorrecto, incluso en un metodo void (§12.5)
class Aa {
    void v() { }
    void m() {
        return v(); // <-
    }
}
