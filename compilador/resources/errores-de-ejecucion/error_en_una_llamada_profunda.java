///3&2&1&Error en tiempo de ejecucion: indice fuera de rango
class Init {
    static void bajar(int n, int[] a) {
        debugPrint(n);
        if (n == 1) {
            a[n + 5] = 0;
        }
        bajar(n - 1, a);
    }
    static void main() {
        bajar(3, new int[2]);
    }
}
