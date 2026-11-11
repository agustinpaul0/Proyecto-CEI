///1&2&3&4&5&exitosamente
class Init {
    static void clasificar(int n) {
        if (n < 0) {
            debugPrint(1);
        } else if (n == 0) {
            debugPrint(2);
        } else if (n < 10) {
            debugPrint(3);
        } else {
            debugPrint(4);
        }
    }
    static void main() {
        clasificar(-5);
        clasificar(0);
        clasificar(7);
        clasificar(70);
        if (true) debugPrint(5);
        if (false) debugPrint(6);
    }
}
