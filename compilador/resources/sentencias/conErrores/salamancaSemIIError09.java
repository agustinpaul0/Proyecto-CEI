///[Error:x|7]
// CN-SII-59: var no puede coincidir con una local visible de un contenedor (S12.3)
class VarBloque {
    void m1() {
        var x = 1;
        {
            var x = 2;
        }
    }
}
