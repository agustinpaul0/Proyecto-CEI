///[Error:true|5]
// CN-SII-85: el tamano de un arreglo nuevo debe ser int (S12.1)
class TamanoNoInt {
    void m1() {
        var v = new int[true];
    }
}
