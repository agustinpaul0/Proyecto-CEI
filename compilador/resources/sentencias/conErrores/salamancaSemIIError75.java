///[Error:procesar|9]
// CN-SII-124: argumento incompatible en llamada a metodo estatico calificado (S10.2)
class Matematicas {
    static void procesar(int n) {
    }
}
class Uso {
    void m1() {
        Matematicas.procesar("hola");
    }
}
