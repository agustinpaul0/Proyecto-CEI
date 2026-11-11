///[Error:x|8]
// CN-SII-60: declaracion local prohibida como cuerpo directo de if (S12.3)
class IfVarDirecto {
    void m1(boolean c) {
        if (c) {
        }
        if (c)
            var x = 1;
    }
}
