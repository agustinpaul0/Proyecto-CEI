// [SinErrores]
// El iterable del for-each es una expresion completa: puede ser un ternario
// (for (var x : c ? a : b)), en este caso las ramas son arreglos; Java admite
// lo mismo, y aqui la combinacion de fors con ternario esta soportada.
//
class Prueba {
    void main() {
        var a = new int[2];
        var b = new int[3];
        var c = true;
        for (var x : c ? a : b) {
            utilizar(x);
        }
    }

    void utilizar(int n) {
    }
}