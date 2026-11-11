///5&0&0&14&4&30&exitosamente
class Init {
    static void main() {
        var a = new int[5];
        debugPrint(a.length);
        debugPrint(a[0]);
        debugPrint(a[4]);
        var i = 0;
        while (i < a.length) {
            a[i] = i * 2;
            i = i + 1;
        }
        debugPrint(a[1] + a[4] + a[2] + a[0]);
        debugPrint(a[2]);
        var suma = 0;
        i = 0;
        while (i < a.length) {
            suma = suma + (a[i] * 1);
            i = i + 1;
        }
        debugPrint(suma + 10);
    }
}
