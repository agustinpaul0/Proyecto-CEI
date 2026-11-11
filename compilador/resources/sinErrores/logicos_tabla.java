///true&false&false&false&true&true&true&false&false&true&exitosamente
class Init {
    static void main() {
        System.printBln(true && true);
        System.printBln(true && false);
        System.printBln(false && true);
        System.printBln(false && false);
        System.printBln(true || true);
        System.printBln(true || false);
        System.printBln(false || true);
        System.printBln(false || false);
        System.printBln(!true);
        System.printBln(!(!true));
    }
}
