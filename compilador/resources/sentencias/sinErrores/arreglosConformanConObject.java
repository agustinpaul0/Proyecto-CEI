// un arreglo se pasa donde se espera Object (§9.4)
class Aa {
    void recibir(Object valor) { }
    void m() {
        recibir(new int[10]);
        recibir(new String[1][1]);
    }
}
