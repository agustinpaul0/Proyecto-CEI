///1&2&exitosamente
class Base {
    static void main() {
        debugPrint(1);
        Derivada.dos();
    }
}

class Derivada extends Base {
    static void dos() {
        debugPrint(2);
    }
}
