///[Error:new|5]
// CN-SII-99: prohibido instanciar una interfaz (S14.1)
class NewInterfaz {
    void m1() {
        var v = new Figura();
    }
}
interface Figura {
}
