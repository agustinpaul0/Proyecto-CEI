///6&3&7&54&exitosamente
class Init {
    static int[] crear(int n) {
        var a = new int[n];
        var i = 0;
        while (i < n) {
            a[i] = i + 1;
            i = i + 1;
        }
        return a;
    }
    static int sumar(int[] a) {
        var s = 0;
        var i = 0;
        while (i < a.length) {
            s = s + a[i];
            i = i + 1;
        }
        return s;
    }
    static void main() {
        debugPrint(sumar(crear(3)));
        debugPrint(crear(3)[2]);
        var a = crear(4);
        a[0] = a[3] + 3;
        debugPrint(a[0]);
        debugPrint(sumar(a) * 2 + 22);
    }
}
