///[Error:true|5]
// CN-SII-84: el indice de un arreglo debe ser int (S12.1)
class IndiceNoInt {
    void m1(int[] a) {
        var v = a[true];
    }
}
