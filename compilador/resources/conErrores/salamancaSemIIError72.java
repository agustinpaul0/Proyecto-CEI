///[Error:+|7]
// CN-SII-121: no se puede operar con expresiones de tipo void (S9.5)
class OpVoid {
    void mVoid() {
    }
    void m1() {
        var x = 1 + mVoid();
    }
}
