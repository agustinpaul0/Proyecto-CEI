///[Error:==|5]
// CN-SII-77: igualdad prohibida entre tipos primitivos de distinta naturaleza (S12.1)
class CharIntIgualdad {
    boolean m1(char c, int i) {
        return c == i;
    }
}
