///[Error:x|7]
// CN-SII-118: var prohibido como cuerpo directo de else (S12.3)
class ElseVarDirecto {
    void m1(boolean c) {
        if (c) {
        } else
            var x = 1;
    }
}
