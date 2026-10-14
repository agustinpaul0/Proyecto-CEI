///[Error:x|8]
// CN-SII-57: var no se infiere el tipo con una llamada void (S12.3)
class VarVoid {
    void g() {
    }

    void m1() {
        var x = g();
    }
}
