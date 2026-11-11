///[Error:hablar|8]
// CN-SII-115: implementacion de metodo de interfaz con tipo de retorno incompatible (S10.5)
interface Hablador {
    String hablar();
}

class Persona implements Hablador {
    public int hablar() {
        return 1;
    }
}