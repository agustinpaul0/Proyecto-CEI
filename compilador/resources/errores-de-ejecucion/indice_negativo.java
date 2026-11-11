///1&Error en tiempo de ejecucion: indice fuera de rango
class Init {
    static void main() {
        var a = new int[3];
        debugPrint(1);
        debugPrint(a[-1]);
        debugPrint(2);
    }
}
