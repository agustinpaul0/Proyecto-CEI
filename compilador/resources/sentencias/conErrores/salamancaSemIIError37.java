///[Error:[|5]
// CN-SII-86: no se puede indexar una variable que no es arreglo (S12.1)
class IndexarNoArreglo {
    void m1(int x) {
        var v = x[0];
    }
}
