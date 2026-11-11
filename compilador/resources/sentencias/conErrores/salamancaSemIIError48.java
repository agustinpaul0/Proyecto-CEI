///[Error:sumar|8]
// CN-SII-97: argumento que no conforma con el tipo del parametro del metodo (S11.1)
class ArgNoConforma {
    void sumar(int n) {
    }

    void m1() {
        sumar("hola");
    }
}
