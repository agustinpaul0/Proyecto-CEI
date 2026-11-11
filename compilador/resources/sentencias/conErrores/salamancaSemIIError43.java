///[Error:(|8]
// CN-SII-92: prohibido que return devuelva el resultado de una llamada a un metodo void (S12.1)
class RetornoLlamadaVoid {
    void g() {
    }

    int m1() {
        return g();
    }
}
