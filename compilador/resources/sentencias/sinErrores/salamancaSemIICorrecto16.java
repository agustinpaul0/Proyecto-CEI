// CP-SII-11: this como expresion y parentizado
class NodoDos {
    NodoDos obtener() {
        return this;
    }

    void m1() {
        var n = (this);
        var m = this.obtener();
        m = n;
    }
}
