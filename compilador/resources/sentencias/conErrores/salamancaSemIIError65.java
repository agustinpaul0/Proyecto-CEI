///[Error:Persona|7]
// CN-SII-114: clase que no implementa un metodo declarado por la interfaz (S10.5)
interface Hablador {
    void hablar();
}

class Persona implements Hablador {
}