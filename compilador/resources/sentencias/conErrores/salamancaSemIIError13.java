///[Error:this|5]
// CN-SII-63: this prohibido dentro de una expresion parentizada en contexto estatico (S10.4)
class ThisParenStatic {
    static void m1() {
        var x = (this);
    }
}
