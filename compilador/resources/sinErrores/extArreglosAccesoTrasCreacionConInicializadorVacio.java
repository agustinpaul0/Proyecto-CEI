///[SinErrores]
// Acceso sobre una creacion con inicializador vacio: new T[]{}[i]

class Prueba {
    void main() {
        var a = new int[]{}[0];
        var b = new char[]{}[1];
    }
}