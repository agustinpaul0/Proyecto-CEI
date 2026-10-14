///[Error:.|5]
// CN-SII-96: los metodos se invocan solo sobre instancias, no sobre la clase (S11.1)
class InstanciaViaClase {
    void m1() {
        Persona.obtenerEdad();
    }
}
class Persona {
    int obtenerEdad() {
        return 1;
    }
}
