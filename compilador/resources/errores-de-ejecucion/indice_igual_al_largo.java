///Error en tiempo de ejecucion: indice fuera de rango
class Init {
    static void main() {
        var a = new int[3];
        debugPrint(a[a.length]);
    }
}
