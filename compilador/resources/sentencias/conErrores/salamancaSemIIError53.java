///[Error:[|5]
// CN-SII-102: prohibida una sentencia formada por el acceso a un indice (S9)
class SentIndice {
    void m1(int[] personas) {
        personas[0];
    }
}
