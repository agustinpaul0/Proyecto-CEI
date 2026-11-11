///5&Error en tiempo de ejecucion: tamanio de arreglo negativo
class Init {
    static int tam() {
        debugPrint(5);
        return 2 - 3;
    }
    static void main() {
        var a = new boolean[tam()];
    }
}
