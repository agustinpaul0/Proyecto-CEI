///999999999&-1294967297&2147483647&-2147483648&0&exitosamente
class Init {
    static void main() {
        debugPrint(999999999);
        debugPrint(999999999 * 3 + 2);
        var max = (999999999 * 2) + 147483649;
        debugPrint(max);
        debugPrint(max + 1);
        debugPrint(max + 1 + max + 1);
    }
}
