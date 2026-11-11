///1&false&3&true&5&6&false&7&8&true&9&false&exitosamente
class Init {
    static boolean t(int x) {
        debugPrint(x);
        return true;
    }
    static boolean f(int x) {
        debugPrint(x);
        return false;
    }
    static void main() {
        System.printBln(f(1) && t(2));
        System.printBln(t(3) || f(4));
        System.printBln(t(5) && f(6));
        System.printBln(f(7) || t(8));
        System.printBln(!t(9));
    }
}
