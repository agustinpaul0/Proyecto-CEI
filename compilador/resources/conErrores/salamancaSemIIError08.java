///[Error:dato|5]
// CN-SII-58: var no puede coincidir con un parametro de la unidad (S12.3)
class VarParam {
    void m1(int dato) {
        var dato = 1;
    }
}
