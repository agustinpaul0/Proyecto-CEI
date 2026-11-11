///[Error:==|5]
// CN-SII-109: igualdad prohibida entre tipos primitivos de distinta naturaleza (S12.1)
class RefPrimIgualdad {
    boolean m1(String s, int i) {
        return s == i;
    }
}
