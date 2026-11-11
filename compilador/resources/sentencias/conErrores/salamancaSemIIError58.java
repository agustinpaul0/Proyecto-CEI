///[Error:=|5]
// CN-SII-107: la escritura en un elemento de un arreglo usa un valor que no conforma con el tipo del arreglo (S12.1)
class CompArreglo {
    void m1(int[] a) {
        a[0] = true;
    }
}
