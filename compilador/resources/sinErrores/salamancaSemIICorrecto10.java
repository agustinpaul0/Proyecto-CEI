// CP-SII-05: operadores logicos && y || con cortocircuito; ambos operandos se chequean
class Logico {
    boolean izquierda() { return true; }
    boolean derecha() { return false; }

    void m1() {
        var r = (this.izquierda()) && (this.derecha());
        var s = (this.izquierda()) || (this.derecha());
    }
}
