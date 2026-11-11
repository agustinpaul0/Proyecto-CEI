// CP-SII-08: this en un metodo de instancia
class Contador {
    int total;

    Contador mismo() {
        return this;
    }

    void incrementar() {
        this.total = this.total + 1;
    }

    void m1() {
        this.incrementar();
        var c = this.mismo();
        this.total = 0;
    }
}
