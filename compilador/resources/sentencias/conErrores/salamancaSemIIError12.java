///[Error:this|5]
// CN-SII-62: this prohibido en contexto estatico (S10.4)
class ThisEnStatic {
    static void m1() {
        var x = this;
    }
}
