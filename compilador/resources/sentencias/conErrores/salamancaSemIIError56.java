///[Error:(|5]
// CN-SII-105: prohibida una sentencia formada por una expresion parentizada con llamada a metodo (S9)
class SentParen2 {
    void m1() {
        (m1());
    }
}
