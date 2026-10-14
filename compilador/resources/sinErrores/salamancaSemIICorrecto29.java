// CP-SII-24: una llamada void es valida como sentencia
class Vacios {
    void imprimir() {
    }

    void m1() {
        imprimir();
        this.imprimir();
        return;
    }
}
