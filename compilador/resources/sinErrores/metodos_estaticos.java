///1&2&3&creando&4&5&exitosamente
class Util {
    static int uno() {
        return 1;
    }
    static int dos() {
        return uno() + 1;
    }
    static int tres() {
        return Util.dos() + 1;
    }
    static Util crear() {
        System.printSln("creando");
        return new Util();
    }
    static int cuatro() {
        return 4;
    }
}

class Init {
    static void main() {
        debugPrint(Util.uno());
        debugPrint(Util.dos());
        var u = new Util();
        debugPrint(u.tres());
        debugPrint(Util.crear().cuatro());
        debugPrint(Init.cinco());
    }
    static int cinco() {
        return 5;
    }
}
