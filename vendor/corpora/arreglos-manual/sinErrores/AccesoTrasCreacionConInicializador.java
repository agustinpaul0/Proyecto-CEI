///[SinErrores]
// Acceso directo sobre una creacion con inicializador: new T[]{...}[i]

class Prueba {
    void main() {
        var a = new int[]{1, 2, 3}[0];
        var b = new int[]{4, 5}[1];
        var c = new boolean[]{true, false}[0];
    }
}