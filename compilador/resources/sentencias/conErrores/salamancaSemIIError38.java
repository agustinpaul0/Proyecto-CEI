///[Error:[|5]
// CN-SII-87: no se puede indexar una variable de tipo Object (S12.1)
class IndexarObject {
    void m1(Object o) {
        var v = o[0];
    }
}
