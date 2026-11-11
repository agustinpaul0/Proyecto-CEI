///false&true&x&2&exitosamente
class Init {
    static void main() {
        var b = new boolean[2];
        System.printBln(b[0]);
        b[1] = true;
        System.printBln(b[1]);
        var c = new char[2];
        c[0] = 'x';
        System.printCln(c[0]);
        debugPrint(c.length);
    }
}
