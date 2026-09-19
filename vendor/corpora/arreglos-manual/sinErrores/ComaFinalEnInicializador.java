///[SinErrores]
// Coma final permitida al final del inicializador de arreglo (JLS 10.6)

class Prueba {
    void main() {
        var a = new int[]{1, 2,};
    }
}