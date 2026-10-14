///[Error:x|5]
// CN-SII-69: this no se puede modificar (es una referencia, no es ligable) (S12.1)
class ThisLHS {
    void m1() {
        this = x;
    }
}
