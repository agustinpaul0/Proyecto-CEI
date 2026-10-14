///[Error:=|5]
// CN-SII-72: no se puede asignar un String a un int (S12.1)
class StringAint {
    void m1(int n) {
        n = "hola";
    }
}
