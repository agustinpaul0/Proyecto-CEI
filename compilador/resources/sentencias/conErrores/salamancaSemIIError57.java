///[Error:(|5]
// CN-SII-106: prohibida una sentencia formada por una expresion parentizada con receptor this (S9)
class SentParenReceptor {
    void m1() {
        (this).m1();
    }
}
