///[Error:=|5]
// CN-SII-70: no se puede asignar a la longitud de un arreglo (S12.1)
class LengthLHS {
    void m1(int[] a) {
        a.length = 2;
    }
}
