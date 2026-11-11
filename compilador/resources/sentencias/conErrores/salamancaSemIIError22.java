///[Error:=|5]
// CN-SII-71: no se puede asignar un int a un boolean (S12.1)
class IntAboolean {
    void m1(boolean b) {
        b = 1;
    }
}
