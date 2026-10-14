// el cuerpo de un constructor se chequea como el de un metodo de instancia
class Aa {
    int x;
    Aa(int inicial) {
        x = inicial;
        this.x = x + 1;
        var y = this;
        if (x > 0) return;
        m();
    }
    void m() { }
}
