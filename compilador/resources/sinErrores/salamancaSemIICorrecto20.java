// CP-SII-15: una local puede ocultar un atributo
class Ocultar {
    int valor;

    void m1() {
        var valor = 10;
        valor = valor + 1;
    }
}
