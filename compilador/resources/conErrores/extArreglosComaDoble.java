///[Error:,|6]
// Coma doble en el inicializador: falta un elemento intermedio

class Prueba {
    void main() {
        var a = new int[]{1, , 2};
    }
}