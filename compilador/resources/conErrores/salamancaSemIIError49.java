///[Error:imprimir|11]
// CN-SII-98: argumento que es el resultado de una llamada void (S11.1)
class ArgVoid {
    void vacio() {
    }

    void imprimir(int n) {
    }

    void m1() {
        imprimir(vacio());
    }
}
