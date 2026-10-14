///[Error:(|5]
// CN-SII-104: prohibida una sentencia formada por una expresion parentizada con asignacion (S9)
class SentParen1 {
    void m1(int x) {
        (x = 3);
    }
}
