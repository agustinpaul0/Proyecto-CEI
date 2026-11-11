///[Error:x|9]
// CN-SII-61: declaracion local prohibida como cuerpo directo de while (S12.3)
class WhileVarDirecto {
    void m1(boolean c) {
        while (c) {
            c = false;
        }
        while (c)
            var x = 1;
    }
}
