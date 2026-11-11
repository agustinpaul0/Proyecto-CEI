///8&8&9&exitosamente
class Base {
    static int ocho() {
        return 8;
    }
}

class Derivada extends Base {
    static int nueve() {
        return ocho() + 1;
    }
}

class Init {
    static void main() {
        debugPrint(Base.ocho());
        debugPrint(Derivada.ocho());
        debugPrint(Derivada.nueve());
    }
}
