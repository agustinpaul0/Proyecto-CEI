///0&Error en tiempo de ejecucion: indice fuera de rango
class Init {
    static void main() {
        var m = new int[2][4];
        debugPrint(m[1][3]);
        m[1][4] = 7;
    }
}
