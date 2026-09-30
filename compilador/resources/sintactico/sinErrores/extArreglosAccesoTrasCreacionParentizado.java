///[SinErrores]
// Acceso parentizado sobre una creacion fresca: (new T[...])[i]

class Prueba {
    void main() {
        var a = (new int[3])[1];
        var b = (new int[]{1, 2})[0];
        var c = (new int[2][2])[1][0];
    }
}