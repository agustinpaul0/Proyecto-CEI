///7&7&8&8&exitosamente
class Init {
    static void main() {
        var a = 0;
        var b = 0;
        a = (b = 7);
        debugPrint(a);
        debugPrint(b);
        var c = (a = 8);
        debugPrint(c);
        debugPrint(a);
    }
}
